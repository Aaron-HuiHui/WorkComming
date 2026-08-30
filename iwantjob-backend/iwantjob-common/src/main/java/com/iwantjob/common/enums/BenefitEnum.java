package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权益类型
 */
@Getter
@AllArgsConstructor
public enum BenefitEnum {

    MENTOR_CONSULT(0, "导师咨询"),
    ADVANCED_RESUME(1, "高级简历优化"),
    MOCK_INTERVIEW_EXTRA(2, "模拟面试次数"),
    OTHER(3, "其他");

    private final int code;
    private final String desc;

    public static BenefitEnum of(int code) {
        for (BenefitEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知权益码: " + code);
    }
}
