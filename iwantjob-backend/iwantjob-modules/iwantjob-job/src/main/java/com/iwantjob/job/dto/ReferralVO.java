package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内推码视图对象
 */
@Data
@Schema(description = "内推码信息")
public class ReferralVO implements Serializable {

    @Schema(description = "内推记录ID")
    private Long id;

    @Schema(description = "创建人用户ID")
    private Long userId;

    @Schema(description = "职位ID")
    private Long jobId;

    @Schema(description = "内推码")
    private String referralCode;

    @Schema(description = "可用次数上限")
    private Integer maxCount;

    @Schema(description = "已使用次数")
    private Integer usedCount;

    @Schema(description = "状态：1有效/0失效")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
