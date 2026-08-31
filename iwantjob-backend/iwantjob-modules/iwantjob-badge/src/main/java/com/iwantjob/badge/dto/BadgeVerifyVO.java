package com.iwantjob.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业徽章查验结果 VO
 */
@Data
@Schema(description = "企业徽章查验结果")
public class BadgeVerifyVO implements Serializable {

    @Schema(description = "是否校验通过")
    private Boolean valid;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "徽章模板ID")
    private Long badgeId;

    @Schema(description = "徽章名称")
    private String name;

    @Schema(description = "徽章描述")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "稀有度：0-普通,1-稀有,2-史诗")
    private Integer rarity;

    @Schema(description = "获得时间")
    private LocalDateTime earnedAt;

    @Schema(description = "是否已锁定铸造")
    private Integer isLocked;

    @Schema(description = "校验说明")
    private String message;
}
