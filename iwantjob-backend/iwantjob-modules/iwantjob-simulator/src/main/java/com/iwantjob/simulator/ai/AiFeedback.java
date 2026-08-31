package com.iwantjob.simulator.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 即时反馈：用户提交选择后，由大模型基于节点描述与选项给出评价
 */
@Data
public class AiFeedback implements Serializable {

    /** AI 反馈文本（对用户本次选择的评价与建议） */
    private String feedback;

    /** 触发的软技能标签，逗号分隔，如 沟通协作,换位思考 */
    private String softSkillTags;
}
