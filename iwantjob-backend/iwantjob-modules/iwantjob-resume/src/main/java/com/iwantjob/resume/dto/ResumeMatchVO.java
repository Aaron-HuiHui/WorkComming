package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 简历-职位匹配度结果 VO
 */
@Data
@Schema(description = "简历-职位匹配度")
public class ResumeMatchVO implements Serializable {

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "职位ID")
    private Long jobId;

    @Schema(description = "匹配度（0-100，关键词重叠率）")
    private Integer matchScore;

    @Schema(description = "命中关键词列表")
    private List<String> matchedKeywords;

    @Schema(description = "简历关键词总数")
    private int resumeKeywordCount;

    @Schema(description = "职位关键词总数")
    private int jobKeywordCount;
}
