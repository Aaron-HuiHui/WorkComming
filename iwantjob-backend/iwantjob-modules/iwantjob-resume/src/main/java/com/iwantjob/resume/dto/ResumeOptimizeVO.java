package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 简历优化结果 VO
 */
@Data
@Schema(description = "AI简历优化结果")
public class ResumeOptimizeVO implements Serializable {

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "优化类型：0-润色,1-翻译,2-强化")
    private Integer type;

    @Schema(description = "原文（content_json 摘要）")
    private String originalText;

    @Schema(description = "优化后文本")
    private String optimizedText;

    @Schema(description = "AI 反馈说明")
    private String feedback;
}
