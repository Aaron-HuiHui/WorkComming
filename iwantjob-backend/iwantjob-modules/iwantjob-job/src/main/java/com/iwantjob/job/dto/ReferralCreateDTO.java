package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建内推码请求 DTO
 */
@Data
@Schema(description = "创建内推码请求")
public class ReferralCreateDTO implements Serializable {

    @Schema(description = "职位ID", example = "1001")
    @NotNull(message = "职位ID不能为空")
    private Long jobId;

    @Schema(description = "内推可用次数上限（1~100）", example = "10")
    @Min(value = 1, message = "内推次数至少为1")
    @Max(value = 100, message = "内推次数不能超过100")
    private Integer maxCount = 10;
}
