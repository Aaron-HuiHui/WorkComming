package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.dto.JobApplyDTO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.entity.JobApplication;
import com.iwantjob.job.mapper.JobApplicationMapper;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}
