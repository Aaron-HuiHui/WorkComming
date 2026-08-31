package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建面试会话响应：返回会话ID与第一题
 */
@Data
@Schema(description = "面试会话创建结果")
public class InterviewStartVO implements Serializable {

    @Schema(description = "面试会话ID")
    private Long mockId;

    @Schema(description = "面试类型：0技术/1行为/2综合")
    private Integer type;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "目标岗位")
    private String targetJob;

    @Schema(description = "题目总数")
    private Integer totalQuestions;

    @Schema(description = "当前题目（第一题）")
    private QuestionVO currentQuestion;
}
