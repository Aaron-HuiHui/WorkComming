package com.iwantjob.common.event;

import com.iwantjob.common.enums.BadgeCondEnum;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 徽章触发事件
 * 各业务模块在关键行为完成后发布此事件，badge 模块消费并判定是否达成徽章条件。
 * 用法：applicationContext.publishEvent(new BadgeTriggerEvent(userId, BadgeCondEnum.SHARE_EXPERIENCE, refId));
 */
@Getter
@ToString
public class BadgeTriggerEvent implements Serializable {

    private final Long userId;
    private final Integer conditionType;
    private final Long refId;
    private final LocalDateTime occurredAt;

    public BadgeTriggerEvent(Long userId, BadgeCondEnum conditionType, Long refId) {
        this.userId = userId;
        this.conditionType = conditionType.getCode();
        this.refId = refId;
        this.occurredAt = LocalDateTime.now();
    }

    public BadgeTriggerEvent(Long userId, int conditionType, Long refId) {
        this.userId = userId;
        this.conditionType = conditionType;
        this.refId = refId;
        this.occurredAt = LocalDateTime.now();
    }
}
