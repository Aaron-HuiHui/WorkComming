package com.iwantjob.salary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 薪资审核动作
 * 对应 salary_review_log.action 字段
 */
@Getter
@AllArgsConstructor
public enum SalaryReviewActionEnum {

    APPROVE("APPROVE", "通过"),
    REJECT("REJECT", "驳回");

    private final String code;
    private final String desc;

    public static SalaryReviewActionEnum of(String code) {
        if (code == null) {
            throw new IllegalArgumentException("审核动作不能为空");
        }
        for (SalaryReviewActionEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        throw new IllegalArgumentException("未知审核动作: " + code);
    }
}
