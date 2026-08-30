package com.iwantjob.user.service.impl;

import com.iwantjob.common.enums.BenefitEnum;
import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.user.dto.UnlockRecordVO;
import com.iwantjob.user.entity.UnlockRecord;
import com.iwantjob.user.mapper.UnlockRecordMapper;
import com.iwantjob.user.service.PointsService;
import com.iwantjob.user.service.UnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权益解锁服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnlockServiceImpl implements UnlockService {

    private final UnlockRecordMapper unlockRecordMapper;
    private final PointsService pointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnlockRecordVO unlockMentor(Long userId) {
        // 1. 计算扣减积分（PointReasonEnum 中已定义默认值）
        int cost = Math.abs(PointReasonEnum.MENTOR_CONSULT_UNLOCK.getDefaultPoints());

        // 2. 先创建解锁记录（status=1 有效），便于流水关联
        UnlockRecord record = new UnlockRecord();
        record.setUserId(userId);
        record.setBenefit(BenefitEnum.MENTOR_CONSULT.getCode());
        record.setCostPoints(cost);
        record.setStatus(1);
        unlockRecordMapper.insert(record);

        // 3. 扣减积分（乐观锁 + 写流水）
        // 若扣减失败，事务回滚会一并撤销 unlock_record 的插入
        pointsService.deductPoints(userId, cost, PointReasonEnum.MENTOR_CONSULT_UNLOCK, record.getId());

        // 4. 组装返回
        UnlockRecordVO vo = new UnlockRecordVO();
        BeanUtils.copyProperties(record, vo);

        log.info("解锁导师咨询成功: userId={}, recordId={}, cost={}", userId, record.getId(), cost);
        return vo;
    }
}
