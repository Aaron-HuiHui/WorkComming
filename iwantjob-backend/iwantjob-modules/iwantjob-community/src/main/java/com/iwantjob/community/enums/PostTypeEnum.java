package com.iwantjob.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子类型枚举
 * 对应 post.type 字段
 */
@Getter
@AllArgsConstructor
public enum PostTypeEnum {

    QA(0, "问答"),
    INTERVIEW(1, "面经"),
    SKILL_EXCHANGE(2, "技能交换"),
    LIFE(3, "生活互助"),
    OTHER(4, "其他");

    private final int code;
    private final String desc;

    public static PostTypeEnum of(int code) {
        for (PostTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知帖子类型码: " + code);
    }
}
