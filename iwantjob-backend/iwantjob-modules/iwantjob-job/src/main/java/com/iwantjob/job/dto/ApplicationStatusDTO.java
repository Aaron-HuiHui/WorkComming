package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * HR 更新投递状态请求 DTO
 */
@Data
@Schema(description = "投递状态流转请求")
public class ApplicationStatusDTO implements Serializable {

    @Schema(description = "目标状态：0投递成功/1初筛/2面试/3录用/4拒绝", example = "1")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态不合法")
    @Max(value = 4, message = "状态不合法")
    private Integer status;

    @Schema(description = "HR备注（可选）")
    @Size(max = 255, message = "备注长度不能超过255")
    private String hrRemark;

    @Schema(description = "面试时间（格式 yyyy-MM-dd HH:mm，进入面试时填写）", example = "2026-09-05 14:00")
    @Size(max = 32)
    private String interviewTime;

    @Schema(description = "面试地点（可选）")
    @Size(max = 100)
    private String interviewLocation;

    @Schema(description = "面试备注（可选）")
    @Size(max = 255)
    private String interviewNote;
}