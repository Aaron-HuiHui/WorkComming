package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单次选择记录 VO（用于报告）
 */
@Data
@Schema(description = "模拟选择记录")
public class ChoiceVO implements Serializable {

    @Schema(description = "选择ID")
    private Long id;

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点描述快照")
    private String nodeDesc;

    @Schema(description = "用户选择的选项ID")
    private Long optionId;

    @Schema(description = "用户选择的选项文本")
    private String userChoice;

    @Schema(description = "AI 反馈")
    private String aiFeedback;

    @Schema(description = "本次选择触发的软技能标签")
    private String softSkillTags;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
