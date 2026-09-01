package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 候选人列表项 VO（HR 视角：投递记录 + 求职者基本资料摘要）
 */
@Data
@Schema(description = "职位候选人（列表项）")
public class CandidateVO implements Serializable {

    @Schema(description = "投递记录ID")
    private Long id;

    @Schema(description = "职位ID")
    private Long jobId;

    @Schema(description = "求职者用户ID")
    private Long userId;

    @Schema(description = "求职者用户名")
    private String username;

    @Schema(description = "求职者姓名（可能为空）")
    private String realName;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "技能标签")
    private String skills;

    @Schema(description = "投递状态：0投递成功/1初筛/2面试/3录用/4拒绝")
    private Integer status;

    @Schema(description = "投递时间")
    private LocalDateTime appliedAt;
}