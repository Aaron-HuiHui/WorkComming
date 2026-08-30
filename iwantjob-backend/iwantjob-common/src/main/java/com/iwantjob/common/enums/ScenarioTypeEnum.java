package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模拟舱场景类型
 */
@Getter
@AllArgsConstructor
public enum ScenarioTypeEnum {

    ONBOARDING(0, "入职"),
    UPWARD_REPORT(1, "向上汇报"),
    CONFLICT(2, "冲突处理"),
    CROSS_DEPT(3, "跨部门协作");

    private final int code;
    private final String desc;

    public static ScenarioTypeEnum of(int code) {
        for (ScenarioTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知场景类型码: " + code);
    }
}
