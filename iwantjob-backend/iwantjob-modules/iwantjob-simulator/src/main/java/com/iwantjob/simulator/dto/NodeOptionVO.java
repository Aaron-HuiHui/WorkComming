package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 模拟舱节点选项 VO
 */
@Data
@Schema(description = "模拟舱节点选项")
public class NodeOptionVO implements Serializable {

    @Schema(description = "选项ID")
    private Long id;

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "选项文本")
    private String optionText;

    @Schema(description = "触发的软技能标签")
    private String softSkillTags;
}
