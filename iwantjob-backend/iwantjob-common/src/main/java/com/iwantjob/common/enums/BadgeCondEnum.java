package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 徽章触发条件类型
 */
@Getter
@AllArgsConstructor
public enum BadgeCondEnum {

    SHARE_EXPERIENCE(0, "分享面经次数"),
    HELP_OTHERS(1, "帮助他人次数"),
    SALARY_CONTRIBUTE(2, "薪资贡献"),
    SIMULATOR_COMPLETE(3, "模拟舱完成"),
    PROJECT_REVIEW(4, "项目合作评价");

    private final int code;
    private final String desc;

    public static BadgeCondEnum of(int code) {
        for (BadgeCondEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知徽章条件码: " + code);
    }
}
