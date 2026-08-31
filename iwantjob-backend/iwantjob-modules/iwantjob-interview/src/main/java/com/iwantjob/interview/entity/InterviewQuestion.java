package com.iwantjob.interview.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试题目表 interview_question
 * <p>该表无 updated_at / is_deleted 字段，题目提交后只更新答案与反馈，不做软删除。</p>
 */
@Data
@TableName("interview_question")
public class InterviewQuestion implements Serializable {

    @TableId
    private Long id;

    /**
     * 所属模拟面试ID
     */
    private Long mockId;

    private String questionText;

    /**
     * 用户回答
     */
    private String answerText;

    /**
     * AI 反馈
     */
    private String aiFeedback;

    /**
     * 参考答案
     */
    private String referenceAnswer;

    /**
     * 题目顺序（从1开始）
     */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
