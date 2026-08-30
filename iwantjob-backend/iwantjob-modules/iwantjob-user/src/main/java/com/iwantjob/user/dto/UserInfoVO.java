package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 VO（含 profile）
 */
@Data
@Schema(description = "当前用户信息")
public class UserInfoVO implements Serializable {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "角色：0学生/1校友/2HR/3导师/9管理员")
    private Integer role;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "状态：0禁用/1正常")
    private Integer status;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLogin;

    // ====== profile 字段 ======

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "技能")
    private String skills;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "求职状态：1求职中/0不求职")
    private Integer availableStatus;

    @Schema(description = "默认简历ID")
    private Long resumeId;
}
