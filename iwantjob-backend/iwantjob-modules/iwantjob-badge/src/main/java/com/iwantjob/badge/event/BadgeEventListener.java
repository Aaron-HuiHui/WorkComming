package com.iwantjob.badge.event;

import com.iwantjob.badge.service.BadgeService;
import com.iwantjob.common.event.BadgeTriggerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 徽章触发事件监听器
 * <p>
 * 使用 {@link TransactionalEventListener} 的 {@link TransactionPhase#AFTER_COMMIT} 阶段消费，
 * 确保业务模块（community/salary/simulator/helpgroup）的事务成功提交后再触发徽章铸造，
 * 避免业务回滚但徽章已铸造的数据不一致问题。
 * <p>
 * 铸造流程：
 * 1. Redis INCR 计数器 key=badge:count:{userId}:{conditionType}
 * 2. 查 badge_template 匹配 condition_type，达 threshold 且未持有 → 铸造
 * 3. lock_hash=SHA256(userId+badgeId+earnedAt+salt)，INSERT user_badge + badge_lock_log
 * <p>
 * 详见 {@link BadgeService#handleTriggerEvent}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeEventListener {

    private final BadgeService badgeService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBadgeTrigger(BadgeTriggerEvent event) {
        if (event == null) {
            return;
        }
        log.info("收到徽章触发事件: userId={}, conditionType={}, refId={}, occurredAt={}",
                event.getUserId(), event.getConditionType(), event.getRefId(), event.getOccurredAt());
        try {
            badgeService.handleTriggerEvent(event.getUserId(), event.getConditionType(), event.getRefId());
        } catch (Exception e) {
            // 监听器异常不应影响发布方，仅记录日志
            log.error("处理徽章触发事件异常: userId={}, conditionType={}, refId={}, err={}",
                    event.getUserId(), event.getConditionType(), event.getRefId(), e.getMessage(), e);
        }
    }
}
