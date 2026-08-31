package com.iwantjob.salary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 薪资数据审核状态
 * 对应 salary_report_data.verified 字段
 */
@Getter
@AllArgsConstructor
public enum SalaryVerifiedEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回");

    private final int code;
    private final String desc;

    public static SalaryVerifiedEnum of(int code) {
        for (SalaryVerifiedEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("未知审核状态码: " + code);
    }
}
