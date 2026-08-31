package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 薪资贡献提交 DTO（脱敏数据）
 * 不收集姓名/身份证/公司全称
 */
@Data
@Schema(description = "薪资贡献提交（脱敏数据）")
public class SalaryContributeDTO implements Serializable {

    @Schema(description = "城市", example = "北京")
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Schema(description = "岗位", example = "Java开发工程师")
    @NotBlank(message = "岗位不能为空")
    @Size(max = 100, message = "岗位长度不能超过100")
    private String position;

    @Schema(description = "薪资下限（元/月）", example = "8000")
    @NotNull(message = "薪资下限不能为空")
    @Min(value = 0, message = "薪资下限不能为负")
    private Integer salaryMin;

    @Schema(description = "薪资上限（元/月）", example = "12000")
    @NotNull(message = "薪资上限不能为空")
    @Min(value = 0, message = "薪资上限不能为负")
    private Integer salaryMax;

    @Schema(description = "公司规模档位（如 1000+、500-1000），不填公司全称", example = "1000+")
    @Size(max = 30, message = "公司规模档位长度不能超过30")
    private String companyScale;

    @Schema(description = "行业", example = "互联网")
    @Size(max = 50, message = "行业长度不能超过50")
    private String industry;

    @Schema(description = "职位类型：0-实习,1-校招,2-社招", example = "1")
    @NotNull(message = "职位类型不能为空")
    @Min(value = 0, message = "职位类型非法")
    @Max(value = 2, message = "职位类型非法")
    private Integer jobType;

    @Schema(description = "学历层次：0-专科,1-本科,2-硕士,3-博士,4-其他", example = "1")
    @NotNull(message = "学历层次不能为空")
    @Min(value = 0, message = "学历层次非法")
    @Max(value = 4, message = "学历层次非法")
    private Integer educationLevel;

    @Schema(description = "是否双一流：0-否,1-是", example = "1")
    @Min(value = 0, message = "双一流标识非法")
    @Max(value = 1, message = "双一流标识非法")
    private Integer isDoubleFirstClass;

    @Schema(description = "offer月份，格式 yyyy-MM", example = "2026-07")
    @NotBlank(message = "offer月份不能为空")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "offer月份格式应为 yyyy-MM")
    private String offerMonth;
}
