package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交选择返回 VO：含 AI 即时反馈与下一节点
 */
@Data
@Schema(description = "提交选择响应")
public class ChooseVO implements Serializable {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "本次选择ID")
    private Long choiceId;

    @Schema(description = "AI 即时反馈")
    private String aiFeedback;

    @Schema(description = "本次选择触发的软技能标签")
    private String softSkillTags;

    @Schema(description = "是否已到达结束节点")
    private Boolean finished;

    @Schema(description = "下一节点（finished=true 时为空）")
    private NodeVO nextNode;

    @Schema(description = "会话状态：0-进行中,1-已完成,2-中断")
    private Integer status;

    @Schema(description = "完成时的综合得分（仅 finished=true 时返回）")
    private Integer overallScore;
}
