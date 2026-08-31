package com.iwantjob.badge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 徽章模板表 badge_template
 * <p>
 * condition_type 取值见 {@link com.iwantjob.common.enums.BadgeCondEnum}：
 * 0-分享面经,1-帮助他人,2-薪资贡献,3-模拟舱完成,4-项目合作评价
 * <p>
 * rarity 取值见 {@link com.iwantjob.common.enums.RarityEnum}：0-普通,1-稀有,2-史诗
 */
@Data
@TableName("badge_template")
public class BadgeTemplate implements Serializable {

    @TableId
    private Long id;

    private String name;

    private String description;

    private String iconUrl;

    /**
     * 触发条件类型 BadgeCondEnum：0-分享面经,1-帮助他人,2-薪资贡献,3-模拟舱完成,4-项目合作评价
     */
    private Integer conditionType;

    /**
     * 达成阈值（次）
     */
    private Integer threshold;

    /**
     * 稀有度 RarityEnum：0-普通,1-稀有,2-史诗
     */
    private Integer rarity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
