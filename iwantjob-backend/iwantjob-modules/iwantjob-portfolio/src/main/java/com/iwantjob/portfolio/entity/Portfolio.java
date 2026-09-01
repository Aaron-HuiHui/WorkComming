package com.iwantjob.portfolio.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品集表 portfolio
 */
@Data
@TableName("portfolio")
public class Portfolio implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private String title;

    private String description;

    /** 封面（emoji） */
    private String cover;

    private String repoUrl;

    private String demoUrl;

    /** 技术标签（逗号分隔） */
    private String techTags;

    private Integer viewCount;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}