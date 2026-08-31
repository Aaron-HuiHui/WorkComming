package com.iwantjob.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 众筹项目状态枚举
 * 对应 crowdfunding_project.status 字段
 */
@Getter
@AllArgsConstructor
public enum CrowdfundingStatusEnum {

    ACTIVE(0, "进行中"),
    SUCCESS(1, "已成功"),
    FAILED(2, "已失败"),
    CANCELED(3, "已取消");

    private final int code;
    private final String desc;

    public static CrowdfundingStatusEnum of(int code) {
        for (CrowdfundingStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知众筹状态码: " + code);
    }
}
