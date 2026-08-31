package com.iwantjob.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.common.event.PointChangeEvent;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.salary.dto.PendingSalaryVO;
import com.iwantjob.salary.dto.SalaryReviewDTO;
import com.iwantjob.salary.dto.SalaryReviewLogVO;
import com.iwantjob.salary.entity.SalaryContributionReward;
import com.iwantjob.salary.entity.SalaryReportData;
import com.iwantjob.salary.entity.SalaryReviewLog;
import com.iwantjob.salary.enums.SalaryReviewActionEnum;
import com.iwantjob.salary.enums.SalaryVerifiedEnum;
import com.iwantjob.salary.mapper.SalaryContributionRewardMapper;
import com.iwantjob.salary.mapper.SalaryReportDataMapper;
import com.iwantjob.salary.mapper.SalaryReviewLogMapper;
import com.iwantjob.salary.service.SalaryReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 薪资审核服务实现
 * 关键点：
 * 1. 待审核列表含 3σ 异常标记
 * 2. APPROVE 发 PointChangeEvent(30分) + 写 contribution_reward
 * 3. 连续 ≥3 次有效贡献解锁 unlock_match_boost
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryReviewServiceImpl implements SalaryReviewService {

    private final SalaryReportDataMapper salaryReportDataMapper;
    private final SalaryReviewLogMapper salaryReviewLogMapper;
    private final SalaryContributionRewardMapper salaryContributionRewardMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** 有效贡献次数阈值，达到后解锁精准匹配优先权 */
    private static final int MATCH_BOOST_THRESHOLD = 3;

    /** 薪资贡献奖励积分 */
    private static final int CONTRIBUTE_REWARD_POINTS = 30;

    /** 3σ 倍数 */
    private static final double ANOMALY_SIGMA = 3.0;

    @Override
    public PageResult<PendingSalaryVO> getPendingList(long page, long size) {
        Page<SalaryReportData> pageParam = new Page<>(page, size);
        Page<SalaryReportData> result = salaryReportDataMapper.selectPage(pageParam,
                new LambdaQueryWrapper<SalaryReportData>()
                        .eq(SalaryReportData::getVerified, SalaryVerifiedEnum.PENDING.getCode())
                        .orderByAsc(SalaryReportData::getCreatedAt));

        List<PendingSalaryVO> voList = result.getRecords().stream()
                .map(this::toPendingVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long reviewerId, Long reportDataId, SalaryReviewDTO dto) {
        // 1. 校验薪资数据存在且为待审核状态
        SalaryReportData data = salaryReportDataMapper.selectById(reportDataId);
        if (data == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "薪资数据不存在");
        }
        if (data.getVerified() != null && data.getVerified() != SalaryVerifiedEnum.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该数据已审核，不可重复审核");
        }

        SalaryReviewActionEnum action = SalaryReviewActionEnum.of(dto.getAction());

        // 2. 写审核日志
        SalaryReviewLog reviewLog = new SalaryReviewLog();
        reviewLog.setReportDataId(reportDataId);
        reviewLog.setReviewerId(reviewerId);
        reviewLog.setAction(action.getCode());
        reviewLog.setComment(dto.getComment());
        salaryReviewLogMapper.insert(reviewLog);

        // 3. 根据动作处理
        if (action == SalaryReviewActionEnum.APPROVE) {
            handleApprove(data, reviewerId);
        } else {
            handleReject(data);
        }

        log.info("薪资审核完成: reportDataId={}, action={}, reviewerId={}",
                reportDataId, action.getCode(), reviewerId);
    }

    @Override
    public List<SalaryReviewLogVO> getReviewLogs(Long reportDataId) {
        List<SalaryReviewLog> logs = salaryReviewLogMapper.selectList(
                new LambdaQueryWrapper<SalaryReviewLog>()
                        .eq(SalaryReviewLog::getReportDataId, reportDataId)
                        .orderByDesc(SalaryReviewLog::getCreatedAt));
        return logs.stream()
                .map(this::toReviewLogVO)
                .collect(Collectors.toList());
    }

    /**
     * 处理审核通过
     * - 更新 verified=1
     * - 发 PointChangeEvent 奖励 30 分
     * - 写 contribution_reward（连续 ≥3 次解锁精准匹配）
     */
    private void handleApprove(SalaryReportData data, Long reviewerId) {
        // 1. 更新审核状态
        SalaryReportData update = new SalaryReportData();
        update.setId(data.getId());
        update.setVerified(SalaryVerifiedEnum.APPROVED.getCode());
        salaryReportDataMapper.updateById(update);

        // 2. 统计用户有效贡献次数（含本次，已更新为通过）
        int approvedCount = salaryReportDataMapper.countApprovedByUser(data.getUserId());
        boolean unlockBoost = approvedCount >= MATCH_BOOST_THRESHOLD;

        // 3. 写贡献奖励记录
        SalaryContributionReward reward = new SalaryContributionReward();
        reward.setUserId(data.getUserId());
        reward.setReportDataId(data.getId());
        reward.setPointsAwarded(CONTRIBUTE_REWARD_POINTS);
        reward.setUnlockMatchBoost(unlockBoost ? 1 : 0);
        salaryContributionRewardMapper.insert(reward);

        // 4. 发 PointChangeEvent（user 模块消费，异步加积分）
        PointChangeEvent event = new PointChangeEvent(
                data.getUserId(),
                CONTRIBUTE_REWARD_POINTS,
                PointReasonEnum.SALARY_CONTRIBUTE.getDesc(),
                data.getId());
        eventPublisher.publishEvent(event);

        log.info("薪资审核通过: reportDataId={}, userId={}, points={}, unlockBoost={}, approvedCount={}",
                data.getId(), data.getUserId(), CONTRIBUTE_REWARD_POINTS, unlockBoost, approvedCount);
    }

    /**
     * 处理审核驳回
     * - 仅更新 verified=2
     */
    private void handleReject(SalaryReportData data) {
        SalaryReportData update = new SalaryReportData();
        update.setId(data.getId());
        update.setVerified(SalaryVerifiedEnum.REJECTED.getCode());
        salaryReportDataMapper.updateById(update);
    }

    /**
     * 实体转待审核 VO，并计算 3σ 异常标记
     */
    private PendingSalaryVO toPendingVO(SalaryReportData data) {
        PendingSalaryVO vo = new PendingSalaryVO();
        vo.setId(data.getId());
        vo.setCity(data.getCity());
        vo.setPosition(data.getPosition());
        vo.setSalaryMin(data.getSalaryMin());
        vo.setSalaryMax(data.getSalaryMax());
        vo.setCompanyScale(data.getCompanyScale());
        vo.setIndustry(data.getIndustry());
        vo.setJobType(data.getJobType());
        vo.setEducationLevel(data.getEducationLevel());
        vo.setIsDoubleFirstClass(data.getIsDoubleFirstClass());
        vo.setOfferMonth(data.getOfferMonth());
        vo.setVerified(data.getVerified());
        vo.setCreatedAt(data.getCreatedAt());
        vo.setAnomalyFlag(checkAnomaly(data));
        return vo;
    }

    /**
     * 3σ 异常检测
     * 基于同城市/同岗位的历史已审核数据计算均值与标准差，
     * 若当前数据薪资均值超出 μ ± 3σ，标记为异常需人工复核。
     * 历史样本不足（<2）时不标记。
     */
    private boolean checkAnomaly(SalaryReportData data) {
        if (data.getCity() == null || data.getPosition() == null
                || data.getSalaryMin() == null || data.getSalaryMax() == null) {
            return false;
        }

        List<Integer> historical = salaryReportDataMapper.selectApprovedAvgSalaries(
                data.getCity(), data.getPosition());
        if (historical == null || historical.size() < 2) {
            return false;
        }

        // 计算均值
        double mean = historical.stream().mapToInt(Integer::intValue).average().orElse(0);
        // 计算标准差
        double variance = historical.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        double stddev = Math.sqrt(variance);
        if (stddev == 0) {
            return false;
        }

        // 当前数据的薪资均值
        int currentAvg = (data.getSalaryMin() + data.getSalaryMax()) / 2;
        double lowerBound = mean - ANOMALY_SIGMA * stddev;
        double upperBound = mean + ANOMALY_SIGMA * stddev;

        return currentAvg < lowerBound || currentAvg > upperBound;
    }

    private SalaryReviewLogVO toReviewLogVO(SalaryReviewLog log) {
        SalaryReviewLogVO vo = new SalaryReviewLogVO();
        vo.setId(log.getId());
        vo.setReportDataId(log.getReportDataId());
        vo.setReviewerId(log.getReviewerId());
        vo.setAction(log.getAction());
        vo.setComment(log.getComment());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
