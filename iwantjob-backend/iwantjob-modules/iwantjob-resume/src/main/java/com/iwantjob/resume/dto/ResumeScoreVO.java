package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 简历评分结果 VO
 */
@Data
@Schema(description = "AI简历评分结果")
public class ResumeScoreVO implements Serializable {

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "AI评分（0-100）")
    private Integer aiScore;

    @Schema(description = "评分反馈说明")
    private String feedback;
}
