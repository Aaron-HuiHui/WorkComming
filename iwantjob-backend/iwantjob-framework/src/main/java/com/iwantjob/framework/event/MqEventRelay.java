package com.iwantjob.framework.event;

import com.iwantjob.common.event.BadgeTriggerEvent;
import com.iwantjob.common.event.BadgeTriggerMessage;
import com.iwantjob.common.event.PointChangeEvent;
import com.iwantjob.common.event.PointChangeMessage;
import com.iwantjob.framework.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 进程内事件 → RabbitMQ 中继
 * <p>
 * 业务模块（community/salary/simulator/helpgroup）保持原有 ApplicationEventPublisher 发布方式不变；
 * 本类在事务提交后（AFTER_COMMIT）将事件序列化为 JSON 投递到 iwantjob.event 交换机，
 * 保留「业务回滚则事件不发出」的语义，同时获得跨进程投递与削峰能力。
 * <p>
 * MQ 不可用时仅记录错误日志，不影响业务主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqEventRelay {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void relayBadgeTrigger(BadgeTriggerEvent event) {
        if (event == null) {
            return;
        }
        send(RabbitMqConfig.RK_BADGE,
                new BadgeTriggerMessage(event.getUserId(), event.getConditionType(), event.getRefId()),
                "BadgeTrigger");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void relayPointChange(PointChangeEvent event) {
        if (event == null) {
            return;
        }
        send(RabbitMqConfig.RK_POINT,
                new PointChangeMessage(event.getUserId(), event.getPoints(), event.getReason(), event.getRefId()),
                "PointChange");
    }

    private void send(String routingKey, Object message, String tag) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, message);
            log.info("事件已中继至MQ: type={}, routingKey={}, payload={}", tag, routingKey, message);
        } catch (Exception e) {
            log.error("事件MQ投递失败(业务主流程不受影响): type={}, routingKey={}, err={}",
                    tag, routingKey, e.getMessage(), e);
        }
    }
}
