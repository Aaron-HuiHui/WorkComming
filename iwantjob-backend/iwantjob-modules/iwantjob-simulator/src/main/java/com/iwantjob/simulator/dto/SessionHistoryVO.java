package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的模拟历史列表项 VO
 */
@Data
@Schema(description = "我的模拟历史")
public class SessionHistoryVO implements Serializable {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "场景ID")
    private Long scenarioId;

    @Schema(description = "场景标题")
    private String scenarioTitle;

    @Schema(description = "场景类型描述")
    private String scenarioTypeDesc;

    @Schema(description = "会话状态：0-进行中,1-已完成,2-中断")
    private Integer status;

    @Schema(description = "综合得分（进行中为空）")
    private Integer overallScore;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}
