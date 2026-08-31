package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模拟会话报告 VO（含所有 choice 与评分）
 */
@Data
@Schema(description = "模拟会话报告")
public class SessionReportVO implements Serializable {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "场景ID")
    private Long scenarioId;

    @Schema(description = "场景标题")
    private String scenarioTitle;

    @Schema(description = "场景类型描述")
    private String scenarioTypeDesc;

    @Schema(description = "会话状态：0-进行中,1-已完成,2-中断")
    private Integer status;

    @Schema(description = "综合得分")
    private Integer overallScore;

    @Schema(description = "各软技能维度得分")
    private Map<String, Integer> dimensionScores;

    @Schema(description = "总体评价")
    private String summary;

    @Schema(description = "改进建议")
    private String suggestions;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "全部选择记录（按时间升序）")
    private List<ChoiceVO> choices;
}
