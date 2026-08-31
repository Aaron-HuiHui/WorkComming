package com.iwantjob.interview.ai;

import com.iwantjob.interview.entity.InterviewQuestion;

import java.util.List;

/**
 * 模拟面试 AI 网关：封装面试场景的 AI 能力（出题 / 评价 / 报告）。
 * <p>
 * 默认实现 {@link DefaultInterviewAiGateway} 返回模拟数据，便于脱离千问配额独立开发与演示；
 * 接入真实大模型时仅需提供新的实现并注入即可，业务层无需改动。
 * </p>
 */
public interface InterviewAiGateway {

    /**
     * 生成单个面试题（题库不足或需要动态出题时使用）
     *
     * @param type       面试类型 0技术/1行为/2综合
     * @param difficulty 难度 1简单/2中等/3困难
     * @param targetJob  目标岗位（可空）
     */
    AiQuestion generateQuestion(Integer type, Integer difficulty, String targetJob);

    /**
     * 评价用户回答，给出反馈与参考答案
     *
     * @param questionText     题目内容
     * @param answerText       用户回答
     * @param expectedKeywords 期望关键词（可空）
     */
    AiEvaluation evaluateAnswer(String questionText, String answerText, String expectedKeywords);

    /**
     * 基于整场面试的问答生成评分报告
     *
     * @param type              面试类型
     * @param difficulty        难度
     * @param targetJob         目标岗位（可空）
     * @param questions         全部题目（含已回答与未回答）
     */
    AiReport generateReport(Integer type, Integer difficulty, String targetJob, List<InterviewQuestion> questions);
}
