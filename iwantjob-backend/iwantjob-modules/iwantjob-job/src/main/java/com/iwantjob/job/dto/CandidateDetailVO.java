package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人详情 VO（HR 查看求职者完整信息）
 * <p>
 * 组合：投递记录 + 求职者基本资料 + 徽章摘要（防篡改背书）+ 投递附带的简历。
 * 简历仅展示投递时附带的那一份（投递即授权），不做全站人才搜索。
 */
@Data
@Schema(description = "候选人详情（HR 视角）")
public class CandidateDetailVO implements Serializable {

    @Schema(description = "投递记录ID")
    private Long applicationId;

    @Schema(description = "职位ID")
    private Long jobId;

    @Schema(description = "职位标题")
    private String jobTitle;

    @Schema(description = "投递状态：0投递成功/1初筛/2面试/3录用/4拒绝")
    private Integer status;

    @Schema(description = "HR备注")
    private String hrRemark;

    @Schema(description = "求职信")
    private String coverLetter;

    @Schema(description = "投递时间")
    private LocalDateTime appliedAt;

    @Schema(description = "面试时间")
    private LocalDateTime interviewTime;

    @Schema(description = "面试地点")
    private String interviewLocation;

    @Schema(description = "面试备注")
    private String interviewNote;

    // ===== 求职者基本信息 =====

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "姓名（可能为空）")
    private String realName;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "技能标签")
    private String skills;

    @Schema(description = "个人简介")
    private String bio;

    // ===== 徽章摘要（防篡改背书） =====

    @Schema(description = "徽章列表")
    private List<BadgeSummary> badges;

    // ===== 投递附带的简历（平铺字段，未附带简历时为空） =====

    @Schema(description = "投递的简历ID（可能为空）")
    private Long resumeId;

    @Schema(description = "简历标题")
    private String resumeTitle;

    @Schema(description = "简历 AI 评分（0-100，未评为空）")
    private Integer resumeAiScore;

    @Schema(description = "简历内容 JSON")
    private String resumeContentJson;

    /**
     * 徽章摘要（公开指纹前8位，与 UserBadgeVO 规则一致）
     */
    @Data
    @Schema(description = "候选人徽章摘要")
    public static class BadgeSummary implements Serializable {

        @Schema(description = "徽章名称")
        private String name;

        @Schema(description = "稀有度：0普通/1稀有/2史诗")
        private Integer rarity;

        @Schema(description = "获得时间")
        private LocalDateTime earnedAt;

        @Schema(description = "防篡改指纹（lock_hash 前8位，未锁定为空）")
        private String fingerprint;
    }
}