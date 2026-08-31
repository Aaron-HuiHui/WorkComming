package com.iwantjob.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户徽章展示 VO（公开主页与个人主页通用）
 * <p>
 * 公开场景仅暴露 lock_hash 前 8 位作为指纹，不暴露完整哈希，
 * 避免被暴力反查；企业查验时通过 /badges/verify 传入完整 hash 校验。
 */
@Data
@Schema(description = "用户徽章")
public class UserBadgeVO implements Serializable {

    @Schema(description = "用户徽章记录ID")
    private Long id;

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

    @Schema(description = "稀有度描述")
    private String rarityDesc;

    @Schema(description = "获得时间")
    private LocalDateTime earnedAt;

    @Schema(description = "是否已锁定铸造：1是,0否")
    private Integer isLocked;

    @Schema(description = "防篡改指纹（lock_hash 前8位）")
    private String fingerprint;
}
