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
 * 题库表 question_bank
 */
@Data
@TableName("question_bank")
public class QuestionBank implements Serializable {

    @TableId
    private Long id;

    /**
     * 分类 InterviewTypeEnum: 0-技术,1-行为,2-综合
     */
    private Integer category;

    /**
     * 子分类（如 Java/算法/沟通能力）
     */
    private String subCategory;

    private String questionText;

    /**
     * 期望关键词（逗号分隔）
     */
    private String expectedKeywords;

    /**
     * 难度：1-简单,2-中等,3-困难
     */
    private Integer difficulty;

    /**
     * 创建人
     */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
