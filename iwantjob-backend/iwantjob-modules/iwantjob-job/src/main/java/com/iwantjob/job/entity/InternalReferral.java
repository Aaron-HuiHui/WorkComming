package com.iwantjob.job.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内推码表 internal_referral
 */
@Data
@TableName("internal_referral")
public class InternalReferral implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private Long jobId;

    private String referralCode;

    private Integer maxCount;

    private Integer usedCount;

    /**
     * 状态：1-有效,0-失效
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
