package com.iwantjob.salary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 薪资审核流转日志表 salary_review_log
 */
@Data
@TableName("salary_review_log")
public class SalaryReviewLog implements Serializable {

    @TableId
    private Long id;

    private Long reportDataId;

    /**
     * 审核人用户ID
     */
    private Long reviewerId;

    /**
     * 审核动作：SalaryReviewActionEnum APPROVE/REJECT
     */
    private String action;

    /**
     * 审核意见
     */
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
