package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分余额 VO
 */
@Data
@Schema(description = "我的积分")
public class PointsVO implements Serializable {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "当前余额")
    private Integer balance;

    @Schema(description = "累计获得")
    private Integer totalEarned;

    public PointsVO(Long userId, Integer balance, Integer totalEarned) {
        this.userId = userId;
        this.balance = balance;
        this.totalEarned = totalEarned;
    }
}
