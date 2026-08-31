package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历视图对象（列表/详情通用）
 */
@Data
@Schema(description = "简历信息")
public class ResumeVO implements Serializable {

    @Schema(description = "简历ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "简历标题")
    private String title;

    @Schema(description = "简历内容 JSON 字符串")
    private String contentJson;

    @Schema(description = "AI评分（0-100），未评分为 null")
    private Integer aiScore;

    @Schema(description = "是否默认简历：1-是,0-否")
    private Integer isDefault;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
