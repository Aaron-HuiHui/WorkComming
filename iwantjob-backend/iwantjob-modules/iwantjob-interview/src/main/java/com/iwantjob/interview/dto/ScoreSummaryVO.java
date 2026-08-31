package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 面试评分汇总（对应 mock_interview.score_summary 的 json 结构）
 */
@Data
@Schema(description = "面试评分汇总")
public class ScoreSummaryVO implements Serializable {

    @Schema(description = "总分（0-100）")
    private Integer overallScore;

    @Schema(description = "各维度得分", example = "{\"技术深度\":82,\"表达清晰度\":85}")
    private Map<String, Integer> dimensionScores;

    @Schema(description = "总体评价")
    private String summary;

    @Schema(description = "改进建议")
    private String suggestions;
}
