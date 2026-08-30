package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权益解锁记录 VO
 */
@Data
@Schema(description = "解锁记录")
public class UnlockRecordVO implements Serializable {

    @Schema(description = "解锁记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "权益类型：0导师咨询,1高级简历优化,2模拟面试次数,3其他")
    private Integer benefit;

    @Schema(description = "消耗积分")
    private Integer costPoints;

    @Schema(description = "状态：1有效/0已使用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "使用时间")
    private LocalDateTime usedAt;
}
