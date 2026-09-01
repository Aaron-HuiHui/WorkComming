package com.iwantjob.badge.event;

import com.iwantjob.badge.service.BadgeService;
import com.iwantjob.common.event.BadgeTriggerMessage;
import com.iwantjob.framework.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 徽章触发事件监听器（RabbitMQ 消费者）
 * <p>
 * 从 iwantjob.badge.trigger 队列消费（发布方为 MqEventRelay 或职位服务等跨进程发布者）。
 * 事件在发布方事务提交后才进入队列，天然规避「业务回滚但徽章已铸造」的不一致。
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

    @RabbitListener(queues = RabbitMqConfig.QUEUE_BADGE)
    public void onBadgeTrigger(BadgeTriggerMessage message) {
        if (message == null || message.getUserId() == null) {
            return;
        }
        log.info("消费徽章触发事件(MQ): userId={}, conditionType={}, refId={}",
                message.getUserId(), message.getConditionType(), message.getRefId());
        try {
            badgeService.handleTriggerEvent(message.getUserId(), message.getConditionType(), message.getRefId());
        } catch (Exception e) {
            // 消费异常仅记录日志，配合 yml 的 retry(3次)+不重回队列，避免毒消息死循环
            log.error("处理徽章触发事件异常: userId={}, conditionType={}, refId={}, err={}",
                    message.getUserId(), message.getConditionType(), message.getRefId(), e.getMessage(), e);
        }
    }
}
