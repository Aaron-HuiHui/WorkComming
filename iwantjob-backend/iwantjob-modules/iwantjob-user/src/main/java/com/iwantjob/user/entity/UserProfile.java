package com.iwantjob.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户资料表 user_profile
 */
@Data
@TableName("user_profile")
public class UserProfile implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private String school;

    private String major;

    private Integer graduationYear;

    private String skills;

    private String bio;

    /**
     * 1-求职中,0-不求职
     */
    private Integer availableStatus;

    private Long resumeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
