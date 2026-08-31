package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 薪资审核请求 DTO
 */
@Data
@Schema(description = "薪资审核请求")
public class SalaryReviewDTO implements Serializable {

    @Schema(description = "审核动作：APPROVE-通过 / REJECT-驳回", example = "APPROVE")
    @NotBlank(message = "审核动作不能为空")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "审核动作只能为 APPROVE 或 REJECT")
    private String action;

    @Schema(description = "审核意见", example = "数据合理，审核通过")
    @Size(max = 255, message = "审核意见长度不能超过255")
    private String comment;
}
