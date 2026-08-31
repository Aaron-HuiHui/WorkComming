package com.iwantjob.interview.ai;

import com.iwantjob.interview.entity.InterviewQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面试 AI 网关默认实现：返回模拟数据。
 * <p>
 * 不依赖任何外部大模型，保证模块可独立编译与本地演示。
 * 真实接入千问时替换为调用 iwantjob-ai 的实现即可。
 * </p>
 */
@Slf4j
@Component
public class DefaultInterviewAiGateway implements InterviewAiGateway {

    private static final int QUESTION_COUNT = 5;

    @Override
    public AiQuestion generateQuestion(Integer type, Integer difficulty, String targetJob) {
        log.debug("【Mock-AI】生成题目: type={}, difficulty={}, targetJob={}", type, difficulty, targetJob);
        AiQuestion q = new AiQuestion();
        String jobSuffix = (targetJob == null || targetJob.isBlank()) ? "" : "（目标岗位：" + targetJob + "）";
        if (type == null || type == 0) {
            q.setQuestionText("请介绍一种你熟悉的排序算法，说明其时间复杂度与适用场景" + jobSuffix + "。");
            q.setReferenceAnswer("以快速排序为例：平均时间复杂度 O(n log n)，最坏 O(n²)；适用于内存排序、大数据量场景，通过分治思想实现。");
            q.setExpectedKeywords("时间复杂度,分治,稳定性,O(n log n)");
        } else if (type == 1) {
            q.setQuestionText("请描述一次你在团队中化解冲突的经历" + jobSuffix + "。");
            q.setReferenceAnswer("采用 STAR 法法：情境(S) - 任务(T) - 行动(A) - 结果(R)，重点说明主动沟通、换位思考与最终共赢结果。");
            q.setExpectedKeywords("STAR,沟通,换位思考,结果");
        } else {
            q.setQuestionText("请谈谈你的职业规划，以及为什么选择这个方向" + jobSuffix + "。");
            q.setReferenceAnswer("短期夯实技术基础，中期成为领域专家，长期向技术管理或架构师发展；结合岗位需求与个人兴趣说明匹配度。");
            q.setExpectedKeywords("职业规划,匹配度,学习能力,目标");
        }
        return q;
    }

    @Override
    public AiEvaluation evaluateAnswer(String questionText, String answerText, String expectedKeywords) {
        log.debug("【Mock-AI】评价回答: questionLen={}, answerLen={}",
                questionText == null ? 0 : questionText.length(),
                answerText == null ? 0 : answerText.length());
        AiEvaluation eval = new AiEvaluation();

        // 参考答案（与题目类型无关的通用占位，真实场景由大模型生成）
        eval.setReferenceAnswer("建议结合 STAR 法则或具体项目案例展开，突出关键技术与量化结果。");

        if (answerText == null || answerText.isBlank()) {
            eval.setFeedback("回答为空，建议补充具体内容。");
            eval.setScore(20);
            return eval;
        }

        int len = answerText.trim().length();
        // 基于回答长度给出基础分（30~85）
        int base = Math.min(85, 30 + len / 8);

        // 命中期望关键词加分（每命中一个 +3，上限 +15）
        int bonus = 0;
        if (expectedKeywords != null && !expectedKeywords.isBlank()) {
            String[] keywords = expectedKeywords.split(",");
            for (String kw : keywords) {
                String k = kw.trim();
                if (!k.isEmpty() && answerText.contains(k)) {
                    bonus = Math.min(15, bonus + 3);
                }
            }
        }
        int score = Math.min(100, base + bonus);

        eval.setScore(score);
        if (score >= 80) {
            eval.setFeedback("回答结构清晰、要点到位，关键词覆盖良好。可进一步补充量化结果以增强说服力。");
        } else if (score >= 60) {
            eval.setFeedback("回答基本覆盖问题，但深度或结构仍有提升空间，建议结合具体案例展开。");
        } else {
            eval.setFeedback("回答过于简略，建议补充背景、做法与结果，体现完整思路。");
        }
        return eval;
    }

    @Override
    public AiReport generateReport(Integer type, Integer difficulty, String targetJob, List<InterviewQuestion> questions) {
        log.debug("【Mock-AI】生成报告: type={}, questionCount={}", type, questions == null ? 0 : questions.size());
        AiReport report = new AiReport();

        int total = questions == null ? 0 : questions.size();
        long answered = questions == null ? 0
                : questions.stream().filter(q -> q.getAnswerText() != null && !q.getAnswerText().isBlank()).count();
        double completion = total == 0 ? 0 : (double) answered / total;

        // 完成度越高得分越高（60 ~ 95）
        int overall = (int) Math.round(60 + completion * 35);

        Map<String, Integer> dims = new HashMap<>();
        dims.put("技术深度", Math.max(0, overall - 5));
        dims.put("表达清晰度", overall);
        dims.put("逻辑性", Math.max(0, overall - 3));
        dims.put("综合表现", Math.min(100, overall + 2));
        report.setDimensionScores(dims);
        report.setOverallScore(overall);

        String typeDesc = type == null ? "综合" : (type == 0 ? "技术" : type == 1 ? "行为" : "综合");
        report.setSummary(String.format(
                "本次%s面试共%d题，完成%d题，完成率%.0f%%，综合得分%d分。",
                typeDesc, total, answered, completion * 100, overall));

        StringBuilder sug = new StringBuilder();
        if (completion < 1.0) {
            sug.append("建议完成全部题目以获得更准确的评估；");
        }
        if (overall < 75) {
            sug.append("回答深度有待加强，建议结合项目案例与量化指标；");
        } else {
            sug.append("整体表现良好，可继续保持并挑战更高难度；");
        }
        sug.append("建议针对薄弱维度进行专项练习。");
        report.setSuggestions(sug.toString());

        return report;
    }
}
