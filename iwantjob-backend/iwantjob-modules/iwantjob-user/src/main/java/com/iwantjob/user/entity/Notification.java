package com.iwantjob.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内通知表 notification（读取侧；职位服务跨服务直写同表）
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    /** 0系统/1投递状态/2面试邀请 */
    private Integer type;

    private String title;

    private String content;

    private Long relatedId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}