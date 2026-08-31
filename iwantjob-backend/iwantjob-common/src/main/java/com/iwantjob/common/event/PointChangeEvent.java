package com.iwantjob.common.event;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分变动事件
 * 各业务模块发布此事件，user 模块消费异步增减积分（避免跨模块直接写积分表）。
 * 用法：applicationContext.publishEvent(new PointChangeEvent(userId, 30, "薪资贡献", refId));
 */
@Getter
@ToString
public class PointChangeEvent implements Serializable {

    private final Long userId;
    private final int points;       // 正数增加，负数扣减
    private final String reason;
    private final Long refId;
    private final LocalDateTime occurredAt;

    public PointChangeEvent(Long userId, int points, String reason, Long refId) {
        this.userId = userId;
        this.points = points;
        this.reason = reason;
        this.refId = refId;
        this.occurredAt = LocalDateTime.now();
    }
}
