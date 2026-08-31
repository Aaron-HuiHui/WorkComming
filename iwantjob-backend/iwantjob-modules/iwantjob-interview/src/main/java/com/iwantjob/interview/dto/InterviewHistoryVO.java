package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试历史列表项
 */
@Data
@Schema(description = "面试历史记录")
public class InterviewHistoryVO implements Serializable {

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

    @Schema(description = "评分汇总（json 字符串）")
    private String scoreSummary;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
