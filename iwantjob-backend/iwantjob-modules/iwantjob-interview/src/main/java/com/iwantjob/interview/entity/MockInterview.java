package com.iwantjob.interview.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模拟面试表 mock_interview
 */
@Data
@TableName("mock_interview")
public class MockInterview implements Serializable {

    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 面试类型 InterviewTypeEnum: 0-技术,1-行为,2-综合
     */
    private Integer type;

    /**
     * 难度：1-简单,2-中等,3-困难
     */
    private Integer difficulty;

    /**
     * 目标岗位
     */
    private String targetJob;

    /**
     * 状态 MockStatusEnum: 0-进行中,1-完成,2-中断
     */
    private Integer status;

    /**
     * 评分汇总(json 字符串)
     */
    private String scoreSummary;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
