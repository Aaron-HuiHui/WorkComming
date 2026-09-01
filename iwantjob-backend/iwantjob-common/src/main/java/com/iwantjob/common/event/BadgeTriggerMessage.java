package com.iwantjob.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * BadgeTriggerEvent 的 MQ 消息体（JSON 传输）
 * 进程内事件由 MqEventRelay 在事务提交后转换为本消息投递到 RabbitMQ，
 * 消费侧（badge 模块）从队列还原并处理。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeTriggerMessage implements Serializable {

    private Long userId;
    private Integer conditionType;
    private Long refId;
}
