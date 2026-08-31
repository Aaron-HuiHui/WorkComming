package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 简历优化请求 DTO
 */
@Data
@Schema(description = "AI简历优化请求")
public class ResumeOptimizeDTO implements Serializable {

    @Schema(description = "简历ID")
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @Schema(description = "优化类型：0-润色,1-翻译,2-强化", example = "0")
    @NotNull(message = "优化类型不能为空")
    private Integer type;

    @Schema(description = "目标语言（type=1 翻译时使用，如 en/ja）", example = "en")
    @Size(max = 10, message = "语言代码长度不能超过10")
    private String targetLang;
}
