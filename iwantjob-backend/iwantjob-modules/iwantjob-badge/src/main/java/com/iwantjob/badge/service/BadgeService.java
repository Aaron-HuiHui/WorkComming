package com.iwantjob.badge.service;

import com.iwantjob.badge.dto.BadgeVerifyVO;
import com.iwantjob.badge.dto.UserBadgeVO;

import java.util.List;

/**
 * 徽章业务服务接口
 * <p>
 * 涵盖：事件触发铸造、用户徽章查询、企业查验。
 * 铸造流程由 {@link com.iwantjob.badge.event.BadgeEventListener} 触发。
 */
public interface BadgeService {

    /**
     * 处理徽章触发事件：Redis INCR 计数，达阈值且未持有则铸造。
     * <p>
     * 使用 @TransactionalEventListener(phase=AFTER_COMMIT) 确保业务成功后再铸造。
     *
     * @param userId        用户ID
     * @param conditionType 条件类型 BadgeCondEnum
     * @param refId         业务关联ID（去重用）
     */
    void handleTriggerEvent(Long userId, Integer conditionType, Long refId);

    /**
     * 铸造用户徽章：计算 lock_hash=SHA256(userId+badgeId+earnedAt+salt)，
     * INSERT user_badge(is_locked=1, lock_hash)，INSERT badge_lock_log(action=LOCK, operated_by=null)。
     *
     * @param userId     用户ID
     * @param badgeId    徽章模板ID
     * @return user_badge 记录ID
     */
    Long mintBadge(Long userId, Long badgeId);

    /**
     * 公开主页徽章列表（含 lock_hash 前8位指纹）
     */
    List<UserBadgeVO> listUserBadges(Long userId);

    /**
     * 当前用户徽章列表
     */
    List<UserBadgeVO> listMyBadges(Long userId);

    /**
     * 企业查验：校验传入 hash 与 DB lock_hash 是否一致
     */
    BadgeVerifyVO verify(Long userId, Long badgeId, String hash);
}
