package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模拟舱场景列表项 VO
 */
@Data
@Schema(description = "模拟舱场景列表项")
public class ScenarioVO implements Serializable {

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

    @Schema(description = "难度：1-简单,2-中等,3-困难")
    private Integer difficulty;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
