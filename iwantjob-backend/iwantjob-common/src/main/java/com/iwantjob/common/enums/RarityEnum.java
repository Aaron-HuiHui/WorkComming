package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 徽章稀有度
 */
@Getter
@AllArgsConstructor
public enum RarityEnum {

    COMMON(0, "普通"),
    RARE(1, "稀有"),
    EPIC(2, "史诗");

    private final int code;
    private final String desc;

    public static RarityEnum of(int code) {
        for (RarityEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知稀有度码: " + code);
    }
}
