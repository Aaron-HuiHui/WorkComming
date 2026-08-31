package com.iwantjob.helpgroup.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 求助原因类型枚举
 * 对应 help_group_request.reason_type
 */
@Getter
@AllArgsConstructor
public enum HelpReasonTypeEnum {

    APPLY_FAILED(0, "投递失败"),
    INTERVIEW_FAILED(1, "面试屡败"),
    CAREER_CONFUSED(2, "职业迷茫"),
    OTHER(3, "其他");

    private final int code;
    private final String desc;

    public static HelpReasonTypeEnum of(int code) {
        for (HelpReasonTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知求助原因类型码: " + code);
    }
}
