package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 题目视图（精简，用于会话推进时返回当前/下一题）
 */
@Data
@Schema(description = "面试题目信息")
public class QuestionVO implements Serializable {

    @Schema(description = "问题ID")
    private Long questionId;

    @Schema(description = "题目顺序（从1开始）")
    private Integer sortOrder;

    @Schema(description = "问题内容")
    private String questionText;
}
