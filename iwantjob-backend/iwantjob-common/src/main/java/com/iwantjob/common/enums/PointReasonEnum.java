package com.iwantjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分变动原因
 */
@Getter
@AllArgsConstructor
public enum PointReasonEnum {

    SALARY_CONTRIBUTE("薪资贡献", 30),
    SHARE_EXPERIENCE("分享面经", 10),
    ANSWER_ACCEPTED("回答被采纳", 20),
    HELP_RESOLVE("帮帮团支援完成", 25),
    SIMULATOR_COMPLETE("模拟舱完成", 15),
    MENTOR_CONSULT_UNLOCK("导师咨询解锁", -50),
    ADVANCED_RESUME_UNLOCK("高级简历优化解锁", -30),
    MOCK_INTERVIEW_UNLOCK("模拟面试次数解锁", -20);

    private final String desc;
    private final int defaultPoints; // 正数增加，负数扣减

    /**
     * 按中文描述反查枚举（MQ 事件消息中 reason 以 desc 文案传输）
     */
    public static PointReasonEnum fromDesc(String desc) {
        for (PointReasonEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知积分原因: " + desc);
    }
}
