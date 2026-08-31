package com.iwantjob.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 徽章模板展示 VO
 */
@Data
@Schema(description = "徽章模板")
public class BadgeTemplateVO implements Serializable {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "徽章名称")
    private String name;

    @Schema(description = "徽章描述")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "触发条件类型：0-分享面经,1-帮助他人,2-薪资贡献,3-模拟舱完成,4-项目评价")
    private Integer conditionType;

    @Schema(description = "条件描述（如：分享面经次数）")
    private String conditionDesc;

    @Schema(description = "达成阈值")
    private Integer threshold;

    @Schema(description = "稀有度：0-普通,1-稀有,2-史诗")
    private Integer rarity;

    @Schema(description = "稀有度描述")
    private String rarityDesc;
}
