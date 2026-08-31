package com.iwantjob.community.entity;

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
 * 技能交换实体
 * 对应表 skill_exchange
 */
@Data
@TableName("skill_exchange")
public class SkillExchange implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起者ID */
    private Long fromUserId;

    /** 目标用户ID */
    private Long toUserId;

    /** 提供的技能 */
    private String offerSkill;

    /** 期望换取的技能 */
    private String wantSkill;

    /** SkillExchangeStatusEnum: 0-待响应,1-已接受,2-已拒绝,3-已完成 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
