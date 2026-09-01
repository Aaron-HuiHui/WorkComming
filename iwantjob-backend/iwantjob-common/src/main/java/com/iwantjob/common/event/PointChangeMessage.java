package com.iwantjob.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * PointChangeEvent 的 MQ 消息体（JSON 传输）
 * 进程内事件由 MqEventRelay 在事务提交后转换为本消息投递到 RabbitMQ，
 * 消费侧（user 模块）从队列还原并调用积分服务。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointChangeMessage implements Serializable {

    private Long userId;
    private Integer points;   // 正数增加，负数扣减
    private String reason;
    private Long refId;
}
