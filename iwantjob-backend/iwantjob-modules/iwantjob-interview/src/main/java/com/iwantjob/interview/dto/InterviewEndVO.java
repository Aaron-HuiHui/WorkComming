package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 结束面试响应：返回评分汇总
 */
@Data
@Schema(description = "结束面试结果")
public class InterviewEndVO implements Serializable {

    @Schema(description = "面试会话ID")
    private Long mockId;

    @Schema(description = "状态：1完成")
    private Integer status;

    @Schema(description = "评分汇总")
    private ScoreSummaryVO scoreSummary;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
