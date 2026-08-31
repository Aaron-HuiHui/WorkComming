package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试详情：含会话信息与全部题目
 */
@Data
@Schema(description = "面试详情")
public class InterviewDetailVO implements Serializable {

    @Schema(description = "面试会话ID")
    private Long id;

    @Schema(description = "面试类型：0技术/1行为/2综合")
    private Integer type;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "目标岗位")
    private String targetJob;

    @Schema(description = "状态：0进行中/1完成/2中断")
    private Integer status;

    @Schema(description = "评分汇总")
    private ScoreSummaryVO scoreSummary;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "题目列表（按顺序）")
    private List<QuestionDetailVO> questions;
}
