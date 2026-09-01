package com.iwantjob.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发布/更新作品 DTO
 */
@Data
@Schema(description = "发布作品请求")
public class PortfolioSaveDTO implements Serializable {

    @Schema(description = "作品标题", example = "校园二手交易平台")
    @NotBlank(message = "作品标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @Schema(description = "作品描述")
    @Size(max = 2000, message = "描述长度不能超过2000")
    private String description;

    @Schema(description = "封面 emoji", example = "🚀")
    @Size(max = 50)
    private String cover;

    @Schema(description = "仓库链接")
    @Size(max = 255)
    private String repoUrl;

    @Schema(description = "演示链接")
    @Size(max = 255)
    private String demoUrl;

    @Schema(description = "技术标签（逗号分隔）", example = "Java,SpringBoot,Vue3")
    @Size(max = 200)
    private String techTags;
}