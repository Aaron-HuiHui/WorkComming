package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发布职位请求 DTO
 */
@Data
@Schema(description = "发布职位请求")
public class JobCreateDTO implements Serializable {

    @Schema(description = "职位标题", example = "Java后端开发工程师")
    @NotBlank(message = "职位标题不能为空")
    @Size(max = 100, message = "职位标题长度不能超过100")
    private String title;

    @Schema(description = "公司名称", example = "示例科技有限公司")
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 100, message = "公司名称长度不能超过100")
    private String companyName;

    @Schema(description = "关联企业ID（可选，用于企业主页聚合）")
    private Long companyId;

    @Schema(description = "招聘批次：0日常/1春招/2秋招/3实习批（可选）", example = "2")
    private Integer recruitmentBatch;

    @Schema(description = "职位类型：0实习/1校招/2社招", example = "1")
    @NotNull(message = "职位类型不能为空")
    private Integer jobType;

    @Schema(description = "职位描述")
    private String description;

    @Schema(description = "任职要求")
    private String requirements;

    @Schema(description = "薪资范围", example = "15k-25k")
    @Size(max = 50, message = "薪资范围长度不能超过50")
    private String salaryRange;

    @Schema(description = "工作城市", example = "北京")
    @Size(max = 100, message = "工作城市长度不能超过100")
    private String location;

    @Schema(description = "联系邮箱", example = "hr@example.com")
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String contactEmail;

    @Schema(description = "职位有效期")
    private LocalDateTime expiryDate;
}
