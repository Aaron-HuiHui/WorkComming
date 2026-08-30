package com.iwantjob.user.service.impl;

import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.user.dto.PointsVO;
import com.iwantjob.user.entity.MutualPoints;
import com.iwantjob.user.entity.PointTransaction;
import com.iwantjob.user.mapper.MutualPointsMapper;
import com.iwantjob.user.mapper.PointTransactionMapper;
import com.iwantjob.user.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现
 * 关键点：扣减用乐观锁 + 余额校验，影响行数0时区分"余额不足"与"并发冲突"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final MutualPointsMapper mutualPointsMapper;
    private final PointTransactionMapper pointTransactionMapper;

    @Override
    public PointsVO getMyPoints(Long userId) {
        MutualPoints points = mutualPointsMapper.selectById(userId);
        if (points == null) {
            // 防御性兜底：账户不存在返回0（正常流程注册时已创建）
            return new PointsVO(userId, 0, 0);
        }
        return new PointsVO(userId, points.getBalance(), points.getTotalEarned());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deductPoints(Long userId, int points, PointReasonEnum reason, Long relatedId) {
        if (points <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "扣减积分必须为正数");
        }

        MutualPoints current = mutualPointsMapper.selectById(userId);
        if (current == null) {
            throw new BusinessException(ErrorCode.POINTS_NOT_ENOUGH, "积分账户不存在");
        }

        // 1. 乐观锁扣减
        int affected = mutualPointsMapper.deductPoints(userId, points, current.getVersion());

        if (affected == 0) {
            // 2. 失败时区分原因：再查一次当前余额
            MutualPoints latest = mutualPointsMapper.selectById(userId);
            if (latest == null) {
                throw new BusinessException(ErrorCode.POINTS_NOT_ENOUGH, "积分账户不存在");
            }
            if (latest.getBalance() == null || latest.getBalance() < points) {
                throw new BusinessException(ErrorCode.POINTS_NOT_ENOUGH);
            }
            // 余额够但行数0 → 版本冲突
            throw new BusinessException(ErrorCode.POINTS_CONCURRENT);
        }

        // 3. 写流水（points 用负数表示扣减）
        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setPoints(-points);
        tx.setReason(reason.name());
        tx.setRelatedId(relatedId);
        pointTransactionMapper.insert(tx);

        int newBalance = current.getBalance() - points;
        log.info("扣减积分成功: userId={}, points={}, reason={}, newBalance={}", userId, points, reason.name(), newBalance);
        return newBalance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addPoints(Long userId, int points, PointReasonEnum reason, Long relatedId) {
        if (points <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "增加积分必须为正数");
        }

        MutualPoints current = mutualPointsMapper.selectById(userId);
        if (current == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "积分账户不存在");
        }

        int affected = mutualPointsMapper.addPoints(userId, points, current.getVersion());
        if (affected == 0) {
            // 增加积分只可能因版本号冲突失败
            throw new BusinessException(ErrorCode.POINTS_CONCURRENT);
        }

        // 写流水（正数表示增加）
        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setPoints(points);
        tx.setReason(reason.name());
        tx.setRelatedId(relatedId);
        pointTransactionMapper.insert(tx);

        int newBalance = current.getBalance() + points;
        log.info("增加积分成功: userId={}, points={}, reason={}, newBalance={}", userId, points, reason.name(), newBalance);
        return newBalance;
    }
}
