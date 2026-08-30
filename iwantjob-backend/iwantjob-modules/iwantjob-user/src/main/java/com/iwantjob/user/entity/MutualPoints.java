package com.iwantjob.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分表 mutual_points
 * 主键为 user_id（非自增），无 is_deleted / created_at
 */
@Data
@TableName("mutual_points")
public class MutualPoints implements Serializable {

    @TableId
    private Long userId;

    private Integer balance;

    private Integer totalEarned;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
