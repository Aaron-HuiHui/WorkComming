package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 题库浏览 VO（学生学习中心）
 */
@Data
@Schema(description = "题库题目")
public class QuestionBankVO implements Serializable {

    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "分类：0技术/1行为/2综合")
    private Integer category;

    @Schema(description = "子分类（如 Java/算法/沟通能力）")
    private String subCategory;

    @Schema(description = "题干")
    private String questionText;

    @Schema(description = "难度：1简单/2中等/3困难")
    private Integer difficulty;

    @Schema(description = "考点关键词（仅详情返回，逗号分隔）")
    private String expectedKeywords;
}