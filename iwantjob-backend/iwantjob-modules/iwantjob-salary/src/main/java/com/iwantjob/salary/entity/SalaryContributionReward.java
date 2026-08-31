package com.iwantjob.salary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 薪资贡献奖励表 salary_contribution_reward
 */
@Data
@TableName("salary_contribution_reward")
public class SalaryContributionReward implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    /**
     * 关联的薪资数据ID
     */
    private Long reportDataId;

    /**
     * 已发放积分数
     */
    private Integer pointsAwarded;

    /**
     * 是否解锁精准匹配优先权：0-否,1-是
     * 连续 ≥3 次有效贡献或高可信数据时置1
     */
    private Integer unlockMatchBoost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
