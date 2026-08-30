package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学历层次
 */
@Getter
@AllArgsConstructor
public enum EduEnum {

    COLLEGE(0, "专科"),
    BACHELOR(1, "本科"),
    MASTER(2, "硕士"),
    DOCTOR(3, "博士"),
    OTHER(4, "其他");

    private final int code;
    private final String desc;

    public static EduEnum of(int code) {
        for (EduEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知学历码: " + code);
    }
}
