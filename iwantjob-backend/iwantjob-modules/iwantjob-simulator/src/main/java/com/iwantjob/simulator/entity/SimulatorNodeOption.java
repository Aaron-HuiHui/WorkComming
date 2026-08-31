package com.iwantjob.simulator.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 模拟舱节点选项表 simulator_node_option
 * 选择某选项后按 next_node_id 推进，soft_skill_tags 标记触发的软技能标签
 */
@Data
@TableName("simulator_node_option")
public class SimulatorNodeOption implements Serializable {

    @TableId
    private Long id;

    private Long nodeId;

    /**
     * 选项文本
     */
    private String optionText;

    /**
     * 下一节点ID；为空表示无后续（应配 is_end 节点）
     */
    private Long nextNodeId;

    /**
     * 选择该项触发的软技能标签，逗号分隔，如 沟通协作,换位思考
     */
    private String softSkillTags;
}
