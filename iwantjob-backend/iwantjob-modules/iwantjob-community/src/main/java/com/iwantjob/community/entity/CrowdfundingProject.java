package com.iwantjob.community.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 众筹项目实体
 * 对应表 crowdfunding_project
 */
@Data
@TableName("crowdfunding_project")
public class CrowdfundingProject implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起人ID */
    private Long initiatorId;

    private String title;

    private String description;

    /** 目标金额 */
    private BigDecimal goalAmount;

    /** 当前已筹金额 */
    private BigDecimal currentAmount;

    /** CrowdfundingStatusEnum: 0-进行中,1-已成功,2-已失败,3-已取消 */
    private Integer status;

    /** 截止时间 */
    private LocalDateTime endDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
