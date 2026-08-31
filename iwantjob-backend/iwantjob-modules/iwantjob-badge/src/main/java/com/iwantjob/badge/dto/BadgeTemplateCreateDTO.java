package com.iwantjob.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 新建徽章模板 DTO（管理员）
 */
@Data
@Schema(description = "新建徽章模板请求")
public class BadgeTemplateCreateDTO implements Serializable {

    @Schema(description = "徽章名称", example = "面经分享达人")
    @NotBlank(message = "徽章名称不能为空")
    @Size(max = 50, message = "徽章名称长度不能超过50")
    private String name;

    @Schema(description = "徽章描述", example = "累计分享5篇面经")
    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    @Schema(description = "图标URL", example = "https://cdn.iwantjob.com/badge/share.png")
    @Size(max = 255, message = "图标URL长度不能超过255")
    private String iconUrl;

    @Schema(description = "触发条件类型：0-分享面经,1-帮助他人,2-薪资贡献,3-模拟舱完成,4-项目评价", example = "0")
    @NotNull(message = "条件类型不能为空")
    @Min(value = 0, message = "条件类型不合法")
    private Integer conditionType;

    @Schema(description = "达成阈值", example = "5")
    @NotNull(message = "阈值不能为空")
    @Min(value = 1, message = "阈值至少为1")
    private Integer threshold;

    @Schema(description = "稀有度：0-普通,1-稀有,2-史诗", example = "0")
    @NotNull(message = "稀有度不能为空")
    @Min(value = 0, message = "稀有度不合法")
    private Integer rarity;
}
