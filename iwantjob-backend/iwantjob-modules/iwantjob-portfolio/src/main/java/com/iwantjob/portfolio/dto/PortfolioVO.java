package com.iwantjob.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品集视图对象
 */
@Data
@Schema(description = "学生作品")
public class PortfolioVO implements Serializable {

    @Schema(description = "作品ID")
    private Long id;

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "作者用户名")
    private String authorName;

    @Schema(description = "作者姓名")
    private String authorRealName;

    @Schema(description = "作品标题")
    private String title;

    @Schema(description = "作品描述")
    private String description;

    @Schema(description = "封面 emoji")
    private String cover;

    @Schema(description = "仓库链接")
    private String repoUrl;

    @Schema(description = "演示链接")
    private String demoUrl;

    @Schema(description = "技术标签（逗号分隔）")
    private String techTags;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;
}