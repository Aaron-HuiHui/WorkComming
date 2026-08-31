package com.iwantjob.simulator.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 模拟舱节点表 simulator_node
 * 显式建模分支图：每个节点含情境描述与注入大模型的场景片段提示
 */
@Data
@TableName("simulator_node")
public class SimulatorNode implements Serializable {

    @TableId
    private Long id;

    private Long scenarioId;

    /**
     * 节点情境描述
     */
    private String nodeDesc;

    /**
     * 注入大模型的场景片段提示
     */
    private String aiPromptSnippet;

    /**
     * 是否结束节点：0-否,1-是
     */
    private Integer isEnd;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
