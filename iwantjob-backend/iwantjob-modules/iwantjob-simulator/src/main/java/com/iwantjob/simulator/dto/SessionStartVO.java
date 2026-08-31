package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建模拟会话返回 VO：含起始节点描述与选项
 */
@Data
@Schema(description = "模拟会话起始响应")
public class SessionStartVO implements Serializable {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "场景ID")
    private Long scenarioId;

    @Schema(description = "场景标题")
    private String scenarioTitle;

    @Schema(description = "初始情境上下文")
    private String initialContext;

    @Schema(description = "当前节点")
    private NodeVO currentNode;

    @Schema(description = "会话开始时间")
    private LocalDateTime startedAt;
}
