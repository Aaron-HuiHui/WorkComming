package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目详情（含回答与AI反馈），用于面试详情
 */
@Data
@Schema(description = "面试题目详情")
public class QuestionDetailVO implements Serializable {

    @Schema(description = "问题ID")
    private Long id;

    @Schema(description = "问题内容")
    private String questionText;

    @Schema(description = "用户回答")
    private String answerText;

    @Schema(description = "AI 反馈")
    private String aiFeedback;

    @Schema(description = "参考答案")
    private String referenceAnswer;

    @Schema(description = "题目顺序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
