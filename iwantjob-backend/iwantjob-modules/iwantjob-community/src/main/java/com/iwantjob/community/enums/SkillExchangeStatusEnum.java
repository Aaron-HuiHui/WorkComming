package com.iwantjob.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 技能交换状态枚举
 * 对应 skill_exchange.status 字段
 */
@Getter
@AllArgsConstructor
public enum SkillExchangeStatusEnum {

    PENDING(0, "待响应"),
    ACCEPTED(1, "已接受"),
    REJECTED(2, "已拒绝"),
    COMPLETED(3, "已完成");

    private final int code;
    private final String desc;

    public static SkillExchangeStatusEnum of(int code) {
        for (SkillExchangeStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知技能交换状态码: " + code);
    }
}
