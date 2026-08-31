package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交回答响应：返回该题AI反馈、参考答案及下一题
 */
@Data
@Schema(description = "提交回答结果")
public class InterviewAnswerVO implements Serializable {

    @Schema(description = "面试会话ID")
    private Long mockId;

    @Schema(description = "刚回答的问题ID")
    private Long currentQuestionId;

    @Schema(description = "AI 反馈")
    private String aiFeedback;

    @Schema(description = "参考答案")
    private String referenceAnswer;

    @Schema(description = "是否还有下一题")
    private Boolean hasNext;

    @Schema(description = "下一题（无则为 null）")
    private QuestionVO nextQuestion;
}
