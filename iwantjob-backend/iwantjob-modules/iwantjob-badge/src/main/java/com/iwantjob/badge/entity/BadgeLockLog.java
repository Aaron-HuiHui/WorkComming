package com.iwantjob.badge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 徽章锁定审计日志表 badge_lock_log
 * <p>
 * 铸造时写入一条 action=LOCK 记录，operated_by 为 null 表示系统自动铸造。
 * 作为防篡改审计日志，仅允许 INSERT，不设软删除字段。
 */
@Data
@TableName("badge_lock_log")
public class BadgeLockLog implements Serializable {

    @TableId
    private Long id;

    private Long userBadgeId;

    /**
     * 操作人 ID，null 表示系统自动铸造
     */
    private Long operatedBy;

    /**
     * 操作类型：LOCK
     */
    private String action;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
