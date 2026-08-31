package com.iwantjob.resume.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历表 resume
 * content_json 在 DB 为 JSON 类型，Java 侧以 String 承载（应用层自行序列化/反序列化）
 */
@Data
@TableName("resume")
public class Resume implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private String title;

    /**
     * 简历内容 JSON 字符串（结构由前端约定：基本信息/教育/工作/项目/技能等）
     */
    private String contentJson;

    /**
     * AI 评分（0-100），未评分为 null
     */
    private Integer aiScore;

    /**
     * 是否默认简历：1-默认,0-非默认
     */
    private Integer isDefault;

    /**
     * 版本号，每次更新自增
     */
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
