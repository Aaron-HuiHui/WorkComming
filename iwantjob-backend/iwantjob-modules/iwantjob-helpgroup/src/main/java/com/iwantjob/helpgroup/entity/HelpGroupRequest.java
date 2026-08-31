package com.iwantjob.helpgroup.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帮帮团求助请求实体
 * 对应表 help_group_request
 */
@Data
@TableName("help_group_request")
public class HelpGroupRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 求助者ID */
    private Long applicantId;

    /** HelpReasonTypeEnum: 0-投递失败,1-面试屡败,2-职业迷茫,3-其他 */
    private Integer reasonType;

    /** 详细描述 */
    private String description;

    /** 匹配标签，如 目标行业/城市，支撑自动匹配 */
    private String matchTags;

    /** HelpStatusEnum: 0-待匹配,1-已匹配,2-完成,3-关闭 */
    private Integer status;

    /** 支援者ID */
    private Long supporterId;

    /** 匹配时间 */
    private LocalDateTime matchedAt;

    /** 完成时间 */
    private LocalDateTime resolvedAt;

    /** 求助者反馈 */
    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
