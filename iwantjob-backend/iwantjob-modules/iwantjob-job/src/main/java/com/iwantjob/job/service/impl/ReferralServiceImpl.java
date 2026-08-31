package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.job.dto.ReferralCreateDTO;
import com.iwantjob.job.dto.ReferralVO;
import com.iwantjob.job.entity.InternalReferral;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.mapper.InternalReferralMapper;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 内推服务实现
 * 关键点：用 UUID 前 8 位生成唯一 referral_code，碰撞时重试，由唯一索引最终兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private static final int CODE_LENGTH = 8;
    private static final int MAX_RETRY = 5;

    private final InternalReferralMapper internalReferralMapper;
    private final JobMapper jobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReferralVO createReferral(Long userId, ReferralCreateDTO dto) {
        // 校验职位存在
        Job job = jobMapper.selectById(dto.getJobId());
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }

        InternalReferral referral = new InternalReferral();
        referral.setUserId(userId);
        referral.setJobId(dto.getJobId());
        referral.setMaxCount(dto.getMaxCount());
        referral.setUsedCount(0);
        referral.setStatus(1);

        // 生成唯一内推码（UUID 前 8 位，碰撞重试）
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String code = generateCode();
            // 二次确认未被占用
            Long exists = internalReferralMapper.selectCount(
                    new LambdaQueryWrapper<InternalReferral>()
                            .eq(InternalReferral::getReferralCode, code));
            if (exists != null && exists > 0) {
                continue;
            }
            referral.setReferralCode(code);
            try {
                internalReferralMapper.insert(referral);
                log.info("创建内推码成功: id={}, userId={}, jobId={}, code={}",
                        referral.getId(), userId, dto.getJobId(), code);
                ReferralVO vo = new ReferralVO();
                BeanUtils.copyProperties(referral, vo);
                return vo;
            } catch (DuplicateKeyException e) {
                // 唯一索引兜底，换码重试
                log.warn("内推码碰撞，重试: attempt={}, code={}", attempt, code);
            }
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "内推码生成失败，请重试");
    }

    /**
     * 取 UUID 第一个 '-' 前的 8 位字符（大写）
     */
    private String generateCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, CODE_LENGTH);
    }
}
