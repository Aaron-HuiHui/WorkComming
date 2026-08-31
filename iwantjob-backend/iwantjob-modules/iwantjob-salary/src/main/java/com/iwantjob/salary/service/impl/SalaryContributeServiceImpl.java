package com.iwantjob.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.salary.dto.SalaryContributeDTO;
import com.iwantjob.salary.dto.SalaryContributionVO;
import com.iwantjob.salary.entity.SalaryContributionReward;
import com.iwantjob.salary.entity.SalaryReportData;
import com.iwantjob.salary.mapper.SalaryContributionRewardMapper;
import com.iwantjob.salary.mapper.SalaryReportDataMapper;
import com.iwantjob.salary.service.SalaryContributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 薪资贡献服务实现
 * 关键点：
 * 1. Redis Set 去重（同用户同月同岗位）
 * 2. 薪资范围合理性校验
 * 3. 默认匿名且不可改
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryContributeServiceImpl implements SalaryContributeService {

    private final SalaryReportDataMapper salaryReportDataMapper;
    private final SalaryContributionRewardMapper salaryContributionRewardMapper;
    private final StringRedisTemplate redisTemplate;

    /** Redis 去重 key 前缀：salary:dedup:{userId}:{offerMonth}:{position} */
    private static final String DEDUP_KEY_PREFIX = "salary:dedup:";

    /** 去重 key TTL：约 13 个月，覆盖 offer 月 + 1 年 */
    private static final Duration DEDUP_TTL = Duration.ofDays(400);

    /** 薪资上限合理值（元/月），超过视为异常 */
    private static final int SALARY_MAX_CEILING = 1_000_000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long contribute(Long userId, SalaryContributeDTO dto) {
        // 1. 薪资范围合理性校验
        validateSalaryRange(dto);

        // 2. Redis Set 去重：同用户同月同岗位
        String dedupKey = buildDedupKey(userId, dto.getOfferMonth(), dto.getPosition());
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
        if (Boolean.FALSE.equals(absent)) {
            throw new BusinessException(ErrorCode.SALARY_DUPLICATE);
        }

        // 3. 构建实体并入库（默认匿名、待审核）
        SalaryReportData data = new SalaryReportData();
        data.setUserId(userId);
        data.setCity(dto.getCity());
        data.setPosition(dto.getPosition());
        data.setSalaryMin(dto.getSalaryMin());
        data.setSalaryMax(dto.getSalaryMax());
        data.setCompanyScale(dto.getCompanyScale());
        data.setIndustry(dto.getIndustry());
        data.setJobType(dto.getJobType());
        data.setEducationLevel(dto.getEducationLevel());
        data.setIsDoubleFirstClass(dto.getIsDoubleFirstClass() != null ? dto.getIsDoubleFirstClass() : 0);
        data.setOfferMonth(dto.getOfferMonth());
        data.setIsAnonymous(1);  // 默认匿名，不可改
        data.setVerified(0);     // 待审核

        salaryReportDataMapper.insert(data);
        log.info("薪资贡献提交成功: userId={}, reportDataId={}, city={}, position={}",
                userId, data.getId(), dto.getCity(), dto.getPosition());
        return data.getId();
    }

    @Override
    public PageResult<SalaryContributionVO> getMyContributions(Long userId, long page, long size) {
        Page<SalaryReportData> pageParam = new Page<>(page, size);
        Page<SalaryReportData> result = salaryReportDataMapper.selectPage(pageParam,
                new LambdaQueryWrapper<SalaryReportData>()
                        .eq(SalaryReportData::getUserId, userId)
                        .orderByDesc(SalaryReportData::getCreatedAt));

        java.util.List<SalaryContributionVO> voList = result.getRecords().stream()
                .map(this::toContributionVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 薪资范围合理性校验
     * - salary_min <= salary_max
     * - salary_max <= 合理上限
     */
    private void validateSalaryRange(SalaryContributeDTO dto) {
        if (dto.getSalaryMin() != null && dto.getSalaryMax() != null
                && dto.getSalaryMin() > dto.getSalaryMax()) {
            throw new BusinessException(ErrorCode.SALARY_DATA_INVALID, "薪资下限不能大于上限");
        }
        if (dto.getSalaryMax() != null && dto.getSalaryMax() > SALARY_MAX_CEILING) {
            throw new BusinessException(ErrorCode.SALARY_DATA_INVALID, "薪资上限超出合理范围");
        }
    }

    /**
     * 构建去重 key: salary:dedup:{userId}:{offerMonth}:{position}
     * position 去空格转小写，避免大小写/空格差异绕过去重
     */
    private String buildDedupKey(Long userId, String offerMonth, String position) {
        String normalizedPosition = position == null ? "" : position.trim().toLowerCase();
        return DEDUP_KEY_PREFIX + userId + ":" + offerMonth + ":" + normalizedPosition;
    }

    /**
     * 实体转 VO，并查询是否解锁精准匹配
     */
    private SalaryContributionVO toContributionVO(SalaryReportData data) {
        SalaryContributionVO vo = new SalaryContributionVO();
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

        // 查询是否已解锁精准匹配优先权（仅审核通过的记录有 reward）
        if (data.getVerified() != null && data.getVerified() == 1) {
            SalaryContributionReward reward = salaryContributionRewardMapper.selectOne(
                    new LambdaQueryWrapper<SalaryContributionReward>()
                            .eq(SalaryContributionReward::getReportDataId, data.getId())
                            .last("LIMIT 1"));
            vo.setUnlockMatchBoost(reward != null ? reward.getUnlockMatchBoost() : 0);
        } else {
            vo.setUnlockMatchBoost(0);
        }
        return vo;
    }
}
