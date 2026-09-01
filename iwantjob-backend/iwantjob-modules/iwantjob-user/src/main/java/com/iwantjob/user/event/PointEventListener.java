package com.iwantjob.user.event;

import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.common.event.PointChangeMessage;
import com.iwantjob.framework.config.RabbitMqConfig;
import com.iwantjob.user.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 积分变动事件监听器（RabbitMQ 消费者）
 * <p>
 * 从 iwantjob.point.change 队列消费（发布方为 MqEventRelay 或职位服务等跨进程发布者），
 * 调用 user 模块积分服务异步增减互助积分，避免业务模块直接写积分表。
 * 注：此前 PointChangeEvent 在进程内无消费者（死事件），本次 MQ 化后正式激活积分自动发放链路。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointsService pointsService;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_POINT)
    public void onPointChange(PointChangeMessage message) {
        if (message == null || message.getUserId() == null) {
            return;
        }
        log.info("消费积分变动事件(MQ): userId={}, points={}, reason={}, refId={}",
                message.getUserId(), message.getPoints(), message.getReason(), message.getRefId());
        try {
            PointReasonEnum reason = PointReasonEnum.fromDesc(message.getReason());
            if (message.getPoints() >= 0) {
                pointsService.addPoints(message.getUserId(), message.getPoints(), reason, message.getRefId());
            } else {
                pointsService.deductPoints(message.getUserId(), -message.getPoints(), reason, message.getRefId());
            }
        } catch (Exception e) {
            log.error("处理积分变动事件异常: message={}, err={}", message, e.getMessage(), e);
        }
    }
}
