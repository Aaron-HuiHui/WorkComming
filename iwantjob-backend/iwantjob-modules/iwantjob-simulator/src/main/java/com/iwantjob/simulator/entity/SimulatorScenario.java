package com.iwantjob.simulator.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模拟舱场景表 simulator_scenario
 * ScenarioTypeEnum: 0-入职,1-向上汇报,2-冲突处理,3-跨部门协作
 */
@Data
@TableName("simulator_scenario")
public class SimulatorScenario implements Serializable {

    @TableId
    private Long id;

    private String title;

    /**
     * ScenarioTypeEnum: 0-入职,1-向上汇报,2-冲突处理,3-跨部门协作
     */
    @TableField("type")
    private Integer type;

    private String description;

    /**
     * 初始情境上下文，注入首次节点描述前
     */
    private String initialContext;

    /**
     * 起始节点ID
     */
    private Long startNodeId;

    /**
     * 难度：1-简单,2-中等,3-困难
     */
    private Integer difficulty;

    /**
     * 是否启用：0-下线,1-启用
     */
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
