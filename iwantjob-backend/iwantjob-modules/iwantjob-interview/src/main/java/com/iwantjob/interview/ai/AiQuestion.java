package com.iwantjob.interview.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 生成的题目结果
 */
@Data
public class AiQuestion implements Serializable {

    /** 题目内容 */
    private String questionText;
    /** 参考答案 */
    private String referenceAnswer;
    /** 期望关键词（逗号分隔） */
    private String expectedKeywords;
}
