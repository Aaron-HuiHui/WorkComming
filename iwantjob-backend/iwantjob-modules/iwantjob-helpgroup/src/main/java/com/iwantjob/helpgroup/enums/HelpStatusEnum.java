package com.iwantjob.helpgroup.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帮帮团求助状态枚举
 * 对应 help_group_request.status
 */
@Getter
@AllArgsConstructor
public enum HelpStatusEnum {

    PENDING(0, "待匹配"),
    MATCHED(1, "已匹配"),
    RESOLVED(2, "完成"),
    CLOSED(3, "关闭");

    private final int code;
    private final String desc;

    public static HelpStatusEnum of(int code) {
        for (HelpStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知求助状态码: " + code);
    }
}
