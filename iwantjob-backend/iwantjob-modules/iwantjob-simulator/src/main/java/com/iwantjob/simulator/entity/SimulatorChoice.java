package com.iwantjob.simulator.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模拟舱选择记录表 simulator_choice
 * 每个 choice 立即落 MySQL，记录节点快照、用户选择、AI 反馈与触发的软技能标签
 */
@Data
@TableName("simulator_choice")
public class SimulatorChoice implements Serializable {

    @TableId
    private Long id;

    private Long sessionId;

    private Long nodeId;

    /**
     * 节点描述快照
     */
    private String nodeDesc;

    /**
     * 当时呈现的选项快照（JSON 字符串）
     */
    private String optionsJson;

    /**
     * 用户选择的选项ID
     */
    private Long optionId;

    /**
     * 用户选择的选项文本（冗余，便于直接展示）
     */
    private String userChoice;

    /**
     * AI 即时反馈
     */
    private String aiFeedback;

    /**
     * 本次选择触发的软技能标签
     */
    private String softSkillTags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
