package com.iwantjob.user.service;

import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.user.dto.PointsVO;

/**
 * 积分服务接口
 */
public interface PointsService {

    /**
     * 获取用户积分余额
     */
    PointsVO getMyPoints(Long userId);

    /**
     * 扣减积分（乐观锁），并写流水
     *
     * @param userId    用户ID
     * @param points    扣减数量（正数）
     * @param reason    变动原因
     * @param relatedId 关联业务ID
     * @return 扣减后的余额；失败抛 BusinessException
     */
    Integer deductPoints(Long userId, int points, PointReasonEnum reason, Long relatedId);

    /**
     * 增加积分（乐观锁），并写流水
     *
     * @param userId    用户ID
     * @param points    增加数量（正数）
     * @param reason    变动原因
     * @param relatedId 关联业务ID
     * @return 增加后的余额
     */
    Integer addPoints(Long userId, int points, PointReasonEnum reason, Long relatedId);
}
