package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    STUDENT(0, "学生"),
    ALUMNUS(1, "校友"),
    HR(2, "HR"),
    MENTOR(3, "导师"),
    ADMIN(9, "管理员");

    private final int code;
    private final String desc;

    public static UserRoleEnum of(int code) {
        for (UserRoleEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知角色码: " + code);
    }
}
