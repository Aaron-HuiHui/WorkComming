package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.ApplicationStatusDTO;
import com.iwantjob.job.dto.CandidateDetailVO;
import com.iwantjob.job.dto.CandidateVO;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.dto.JobApplyDTO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.entity.Notification;
import com.iwantjob.job.entity.JobApplication;
import com.iwantjob.job.mapper.JobApplicationMapper;
import com.iwantjob.job.mapper.NotificationMapper;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 职位投递服务实现
 * 关键点：同 user_id + job_id 未删除的申请不能重复，抛 APPLY_DUPLICATE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationMapper jobApplicationMapper;
    private final JobMapper jobMapper;
    private final NotificationMapper notificationMapper;

    /** 允许的状态流转目标值 */
    private static final Set<Integer> VALID_STATUS = Set.of(0, 1, 2, 3, 4);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyJob(Long userId, Long jobId, JobApplyDTO dto) {
        // 1. 校验职位存在
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        // 2. 校验未过期
        if (job.getExpiryDate() != null && job.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.JOB_EXPIRED);
        }
        // 3. 校验重复投递（同 user_id + job_id 未删除）
        Long exists = jobApplicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, userId)
                        .eq(JobApplication::getJobId, jobId));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.APPLY_DUPLICATE);
        }
        // 4. 落库
        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setResumeId(dto.getResumeId());
        app.setCoverLetter(dto.getCoverLetter());
        app.setStatus(0);   // 0-投递成功
        jobApplicationMapper.insert(app);
        log.info("投递职位成功: id={}, userId={}, jobId={}", app.getId(), userId, jobId);
        return app.getId();
    }

    @Override
    public PageResult<JobApplicationVO> getMyApplied(Long userId, long page, long size) {
        Page<JobApplicationVO> p = new Page<>(page, size);
        IPage<JobApplicationVO> result = jobApplicationMapper.selectMyApplied(p, userId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ==================== HR 候选人管理 ====================

    @Override
    public PageResult<CandidateVO> getJobCandidates(Long hrUserId, Long jobId, long page, long size) {
        // 1. 校验职位存在且属于当前 HR（管理员由 Controller 层放行时传 null posterId 跳过）
        requireJobOwner(hrUserId, jobId);
        // 2. 分页查询投递者（SQL 中 poster_id 双保险）
        Page<CandidateVO> p = new Page<>(page, size);
        IPage<CandidateVO> result = jobApplicationMapper.selectJobCandidates(p, jobId, hrUserId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public CandidateDetailVO getCandidateDetail(Long hrUserId, Long appId) {
        // SQL 已内嵌 j.poster_id = #{posterId} 校验，查不到即视为无权或不存在
        CandidateDetailVO vo = jobApplicationMapper.selectCandidateDetail(appId, hrUserId);
        if (vo == null) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        // 徽章摘要
        vo.setBadges(jobApplicationMapper.selectCandidateBadges(vo.getUserId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplicationStatus(Long hrUserId, Long appId, ApplicationStatusDTO dto) {
        // 1. 校验状态值
        if (!VALID_STATUS.contains(dto.getStatus())) {
            throw new BusinessException(ErrorCode.APPLICATION_STATUS_INVALID);
        }
        // 2. 查询投递记录并校验职位归属（联查 poster_id）
        CandidateDetailVO vo = jobApplicationMapper.selectCandidateDetail(appId, hrUserId);
        if (vo == null) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        // 3. 更新状态 + 备注 + 面试日程（进入面试时填写）
        JobApplication app = new JobApplication();
        app.setId(appId);
        app.setStatus(dto.getStatus());
        app.setHrRemark(dto.getHrRemark());
        if (dto.getInterviewTime() != null && !dto.getInterviewTime().isBlank()) {
            app.setInterviewTime(parseInterviewTime(dto.getInterviewTime()));
        }
        if (dto.getInterviewLocation() != null && !dto.getInterviewLocation().isBlank()) {
            app.setInterviewLocation(dto.getInterviewLocation());
        }
        if (dto.getInterviewNote() != null && !dto.getInterviewNote().isBlank()) {
            app.setInterviewNote(dto.getInterviewNote());
        }
        jobApplicationMapper.updateById(app);
        log.info("投递状态更新: appId={}, jobId={}, status={}, hrUserId={}", appId, vo.getJobId(), dto.getStatus(), hrUserId);
        // 4. 站内通知求职者（共享库直写，跨服务解耦）
        sendStatusNotification(vo, dto);
    }

    /**
     * 宽松解析面试时间：支持 "yyyy-MM-dd HH:mm" / "yyyy-MM-ddTHH:mm"
     */
    private java.time.LocalDateTime parseInterviewTime(String s) {
        try {
            return java.time.LocalDateTime.parse(s.trim().replace(' ', 'T'));
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    /**
     * 投递状态变更 → 站内通知（type: 1投递状态 / 2面试邀请）
     */
    private void sendStatusNotification(CandidateDetailVO vo, ApplicationStatusDTO dto) {
        try {
            String statusLabel = switch (dto.getStatus()) {
                case 0 -> "已投递";
                case 1 -> "初筛通过";
                case 2 -> "面试中";
                case 3 -> "已录用";
                case 4 -> "未通过";
                default -> "状态更新";
            };
            StringBuilder content = new StringBuilder()
                    .append("您投递的「").append(vo.getJobTitle()).append("」状态已更新为【")
                    .append(statusLabel).append("】。");
            if (dto.getStatus() == 2 && dto.getInterviewTime() != null && !dto.getInterviewTime().isBlank()) {
                content.append("面试时间：").append(dto.getInterviewTime().trim());
                if (dto.getInterviewLocation() != null && !dto.getInterviewLocation().isBlank()) {
                    content.append("，地点：").append(dto.getInterviewLocation().trim());
                }
                content.append("，请提前准备。");
            }
            Notification n = new Notification();
            n.setUserId(vo.getUserId());
            n.setType(dto.getStatus() == 2 ? 2 : 1);
            n.setTitle(dto.getStatus() == 3 ? "录用通知 🎉" : "投递状态更新");
            n.setContent(content.toString());
            n.setRelatedId(vo.getApplicationId());
            n.setIsRead(0);
            notificationMapper.insert(n);
        } catch (Exception e) {
            // 通知失败不影响主流程
            log.warn("发送状态通知失败: appId={}", vo.getApplicationId(), e);
        }
    }

    /**
     * 校验职位存在且属于当前 HR
     */
    private void requireJobOwner(Long hrUserId, Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        if (!job.getPosterId().equals(hrUserId)) {
            throw new BusinessException(ErrorCode.NOT_JOB_OWNER);
        }
    }
}