package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 职位类型
 */
@Getter
@AllArgsConstructor
public enum JobTypeEnum {

    INTERNSHIP(0, "实习"),
    CAMPUS(1, "校招"),
    SOCIAL(2, "社招");

    private final int code;
    private final String desc;

    public static JobTypeEnum of(int code) {
        for (JobTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知职位类型码: " + code);
    }
}
