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
 * 积分流水表 point_transaction
 * 流水不可软删除，无 is_deleted 字段
 */
@Data
@TableName("point_transaction")
public class PointTransaction implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 正数增加，负数扣减
     */
    private Integer points;

    /**
     * 变动原因，对应 PointReasonEnum.name()
     */
    private String reason;

    /**
     * 关联业务ID（如解锁记录ID、薪资贡献ID等）
     */
    private Long relatedId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
