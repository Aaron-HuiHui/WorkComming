package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 模拟舱场景详情 VO（含起始节点与选项）
 */
@Data
@Schema(description = "模拟舱场景详情（含起始节点）")
public class ScenarioDetailVO implements Serializable {

    @Schema(description = "场景ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型：0-入职,1-向上汇报,2-冲突处理,3-跨部门协作")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "初始情境上下文")
    private String initialContext;

    @Schema(description = "难度：1-简单,2-中等,3-困难")
    private Integer difficulty;

    @Schema(description = "起始节点")
    private NodeVO startNode;

    @Schema(description = "场景全部节点数量")
    private Integer nodeCount;

    @Schema(description = "全部节点列表（便于前端可视化编辑）")
    private List<NodeVO> nodes;
}
