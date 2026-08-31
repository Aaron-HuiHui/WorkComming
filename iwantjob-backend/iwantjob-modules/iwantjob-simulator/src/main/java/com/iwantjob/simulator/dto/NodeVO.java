package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 模拟舱节点 VO（含选项）
 */
@Data
@Schema(description = "模拟舱节点（含选项）")
public class NodeVO implements Serializable {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "场景ID")
    private Long scenarioId;

    @Schema(description = "节点情境描述")
    private String nodeDesc;

    @Schema(description = "是否结束节点：0-否,1-是")
    private Integer isEnd;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "选项列表")
    private List<NodeOptionVO> options;
}
