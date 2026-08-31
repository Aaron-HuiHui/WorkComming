package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交面试回答请求
 */
@Data
@Schema(description = "提交面试回答请求")
public class InterviewAnswerDTO implements Serializable {

    @Schema(description = "面试会话ID")
    @NotNull(message = "面试会话ID不能为空")
    private Long mockId;

    @Schema(description = "问题ID")
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    @Schema(description = "回答内容")
    @NotBlank(message = "回答内容不能为空")
    private String answerText;
}
