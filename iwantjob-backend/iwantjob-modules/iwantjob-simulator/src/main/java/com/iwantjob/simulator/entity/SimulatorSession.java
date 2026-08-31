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
 * 模拟舱会话表 simulator_session
 * SimStatusEnum: 0-进行中,1-已完成,2-中断
 */
@Data
@TableName("simulator_session")
public class SimulatorSession implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private Long scenarioId;

    /**
     * SimStatusEnum: 0-进行中,1-已完成,2-中断
     */
    private Integer status;

    /**
     * 当前节点ID（Redis 缓存热数据）
     */
    private Long currentNodeId;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    /**
     * 完成时由 AI 生成的综合得分 0-100
     */
    private Integer overallScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
