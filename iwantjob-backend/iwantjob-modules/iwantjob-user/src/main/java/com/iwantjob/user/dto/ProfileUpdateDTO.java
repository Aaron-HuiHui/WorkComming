package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 个人资料更新 DTO（所有字段可选）
 */
@Data
@Schema(description = "个人资料更新请求")
public class ProfileUpdateDTO implements Serializable {

    @Schema(description = "学校")
    @Size(max = 100, message = "学校长度不能超过100")
    private String school;

    @Schema(description = "专业")
    @Size(max = 100, message = "专业长度不能超过100")
    private String major;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "技能（逗号分隔）")
    @Size(max = 500, message = "技能长度不能超过500")
    private String skills;

    @Schema(description = "个人简介")
    @Size(max = 500, message = "简介长度不能超过500")
    private String bio;

    @Schema(description = "求职状态：1求职中/0不求职")
    private Integer availableStatus;

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String realName;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL长度不能超过255")
    private String avatarUrl;
}
