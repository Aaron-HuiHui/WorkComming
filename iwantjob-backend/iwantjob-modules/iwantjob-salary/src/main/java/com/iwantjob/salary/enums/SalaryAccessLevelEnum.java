package com.iwantjob.salary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 白皮书访问级别
 * 对应 salary_whitepaper.access_level 字段
 */
@Getter
@AllArgsConstructor
public enum SalaryAccessLevelEnum {

    PUBLIC(0, "公开"),
    CONTRIBUTOR(1, "贡献者专属");

    private final int code;
    private final String desc;

    public static SalaryAccessLevelEnum of(int code) {
        for (SalaryAccessLevelEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知访问级别码: " + code);
    }
}
