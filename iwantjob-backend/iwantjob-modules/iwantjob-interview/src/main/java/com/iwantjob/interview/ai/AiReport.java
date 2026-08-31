package com.iwantjob.interview.ai;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 生成的面试报告
 */
@Data
public class AiReport implements Serializable {

    /** 总分（0-100） */
    private Integer overallScore;
    /** 各维度得分 */
    private Map<String, Integer> dimensionScores;
    /** 总体评价 */
    private String summary;
    /** 改进建议 */
    private String suggestions;
}
