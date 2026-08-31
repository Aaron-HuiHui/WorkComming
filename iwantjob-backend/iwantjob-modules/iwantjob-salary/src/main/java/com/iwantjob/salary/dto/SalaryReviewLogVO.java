package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 薪资审核日志 VO
 */
@Data
@Schema(description = "薪资审核日志")
public class SalaryReviewLogVO implements Serializable {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "薪资数据ID")
    private Long reportDataId;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核动作：APPROVE/REJECT")
    private String action;

    @Schema(description = "审核意见")
    private String comment;

    @Schema(description = "审核时间")
    private LocalDateTime createdAt;
}
