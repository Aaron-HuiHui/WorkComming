package com.iwantjob.badge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户徽章表 user_badge
 * <p>
 * 防篡改约束：DB 触发器禁止 UPDATE/DELETE（建表 SQL 已含），
 * 故本实体不设置 @TableLogic（不可软删除），不设置 updatedAt（不可更新）。
 * Java 层亦不提供任何修改/删除接口，仅允许 INSERT。
 * <p>
 * lock_hash = SHA256(user_id + badge_id + earned_at + system_salt)
 */
@Data
@TableName("user_badge")
public class UserBadge implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private Long badgeId;

    /**
     * 获得时间，用于参与哈希计算
     */
    private LocalDateTime earnedAt;

    /**
     * 铸造后立即置 1，不可回退
     */
    private Integer isLocked;

    /**
     * 锁定哈希指纹（64 位十六进制 SHA-256）
     */
    private String lockHash;

    /**
     * 仅 INSERT 填充，不可更新（DB 触发器禁止 UPDATE）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
