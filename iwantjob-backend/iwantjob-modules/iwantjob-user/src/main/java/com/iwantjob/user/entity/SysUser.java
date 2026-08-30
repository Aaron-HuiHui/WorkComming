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
 * 用户表 sys_user
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    /**
     * 角色：UserRoleEnum 0-学生,1-校友,2-HR,3-导师,9-管理员
     */
    private Integer role;

    private String realName;

    private String avatarUrl;

    /**
     * 0-禁用,1-正常
     */
    private Integer status;

    private LocalDateTime lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
