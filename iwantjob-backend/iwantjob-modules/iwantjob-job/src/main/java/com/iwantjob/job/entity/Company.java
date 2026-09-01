package com.iwantjob.job.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业信息表 company
 */
@Data
@TableName("company")
public class Company implements Serializable {

    @TableId
    private Long id;

    private String name;

    private String industry;

    private String scale;

    private String headquarters;

    /** LOGO emoji */
    private String logo;

    private String intro;

    private String culture;

    private String welfare;

    private String website;

    /** 认领的 HR 用户ID */
    private Long claimedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}