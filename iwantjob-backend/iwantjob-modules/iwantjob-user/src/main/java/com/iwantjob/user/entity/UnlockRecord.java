package com.iwantjob.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权益解锁记录表 unlock_record
 */
@Data
@TableName("unlock_record")
public class UnlockRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * BenefitEnum: 0-导师咨询,1-高级简历优化,2-模拟面试次数,3-其他
     */
    private Integer benefit;

    private Integer costPoints;

    /**
     * 1-有效,0-已使用/失效
     */
    private Integer status;

    private LocalDateTime usedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
