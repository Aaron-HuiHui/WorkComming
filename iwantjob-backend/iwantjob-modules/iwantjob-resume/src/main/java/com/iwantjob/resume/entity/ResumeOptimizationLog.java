package com.iwantjob.resume.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历优化日志表 resume_optimization_log
 * 注：该表无 is_deleted 字段（仅追加，不软删），无 updated_at
 */
@Data
@TableName("resume_optimization_log")
public class ResumeOptimizationLog implements Serializable {

    @TableId
    private Long id;

    private Long resumeId;

    private Long userId;

    private String originalText;

    private String optimizedText;

    /**
     * 优化类型 OptimTypeEnum: 0-润色,1-翻译,2-强化
     */
    private Integer type;

    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
