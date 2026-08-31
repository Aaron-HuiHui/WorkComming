package com.iwantjob.simulator.ai;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 会话报告：到达结束节点后，由大模型基于全程 choices 生成评分
 */
@Data
public class AiReport implements Serializable {

    /** 综合得分 0-100 */
    private Integer overallScore;

    /** 各软技能维度得分（沟通协作/应变能力/抗压/跨部门协作） */
    private Map<String, Integer> dimensionScores;

    /** 总体评价 */
    private String summary;

    /** 改进建议 */
    private String suggestions;
}
