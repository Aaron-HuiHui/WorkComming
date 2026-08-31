package com.iwantjob.interview.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 评价结果
 */
@Data
public class AiEvaluation implements Serializable {

    /** 反馈内容 */
    private String feedback;
    /** 参考答案 */
    private String referenceAnswer;
    /** 单题得分（0-100） */
    private Integer score;
}
