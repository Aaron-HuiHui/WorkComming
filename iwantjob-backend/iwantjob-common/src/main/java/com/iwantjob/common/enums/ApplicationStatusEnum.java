package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 投递状态
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatusEnum {

    APPLIED(0, "投递成功"),
    SCREENING(1, "初筛"),
    INTERVIEW(2, "面试"),
    OFFERED(3, "录用"),
    REJECTED(4, "拒绝");

    private final int code;
    private final String desc;

    public static ApplicationStatusEnum of(int code) {
        for (ApplicationStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知投递状态码: " + code);
    }
}
