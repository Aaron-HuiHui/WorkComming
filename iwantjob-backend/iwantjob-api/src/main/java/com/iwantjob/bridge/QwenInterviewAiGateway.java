package com.iwantjob.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwantjob.ai.AiChatService;
import com.iwantjob.interview.ai.AiEvaluation;
import com.iwantjob.interview.ai.AiQuestion;
import com.iwantjob.interview.ai.AiReport;
import com.iwantjob.interview.ai.InterviewAiGateway;
import com.iwantjob.interview.entity.InterviewQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面试 AI 网关千问真实实现（R1 桥接）。
 * <p>
 * 仅在 ai.qwen.enabled=true 时装配，内部包装 {@link AiChatService}。
 * 出题 / 评价 / 报告均要求模型输出 JSON，解析失败时降级为保守结果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "true")
public class QwenInterviewAiGateway implements InterviewAiGateway {

    private final AiChatService aiChatService;

    @Override
    public AiQuestion generateQuestion(Integer type, Integer difficulty, String targetJob) {
        String typeLabel = type == null ? "综合" : switch (type) {
            case 0 -> "技术";
            case 1 -> "行为";
            default -> "综合";
        };
        String diffLabel = difficulty == null ? "中等" : switch (difficulty) {
            case 1 -> "简单";
            case 3 -> "困难";
            default -> "中等";
        };
        String job = (targetJob == null || targetJob.isBlank()) ? "通用" : targetJob;

        String prompt = "你是一名资深面试官。请针对以下要求出一道面试题。\n"
                + "面试类型：" + typeLabel + "；难度：" + diffLabel + "；目标岗位：" + job + "。\n"
                + "只输出 JSON 对象，格式：\n"
                + "{\"questionText\":\"题目\",\"referenceAnswer\":\"参考答案\","
                + "\"expectedKeywords\":\"关键词1,关键词2\"}";
        JsonNode node = AiJsonExtractor.extractObject(aiChatService.chat(prompt));
        AiQuestion q = new AiQuestion();
        if (node != null) {
            q.setQuestionText(AiJsonExtractor.text(node, "questionText", null));
            q.setReferenceAnswer(AiJsonExtractor.text(node, "referenceAnswer", null));
            q.setExpectedKeywords(AiJsonExtractor.text(node, "expectedKeywords", null));
        }
        if (q.getQuestionText() == null || q.getQuestionText().isBlank()) {
            log.warn("[QwenAI] 面试出题解析失败，使用兜底题目");
            q.setQuestionText("请介绍一个你最有成就感的项目，说明你的角色、做法与结果。");
            q.setReferenceAnswer("采用 STAR 法则展开，突出个人贡献与量化结果。");
            q.setExpectedKeywords("STAR,角色,量化,结果");
        }
        return q;
    }

    @Override
    public AiEvaluation evaluateAnswer(String questionText, String answerText, String expectedKeywords) {
        AiEvaluation eval = new AiEvaluation();
        if (answerText == null || answerText.isBlank()) {
            eval.setFeedback("回答为空，建议补充具体内容。");
            eval.setScore(20);
            return eval;
        }
        String prompt = "你是一名资深面试官，请评价候选人的回答。\n"
                + "【题目】" + questionText + "\n"
                + (expectedKeywords == null || expectedKeywords.isBlank()
                        ? "" : "【期望关键词】" + expectedKeywords + "\n")
                + "【候选人回答】" + answerText + "\n"
                + "只输出 JSON 对象，格式：{\"feedback\":\"评价与改进建议\","
                + "\"referenceAnswer\":\"参考答案要点\",\"score\":0到100的整数}";
        JsonNode node = AiJsonExtractor.extractObject(aiChatService.chat(prompt));
        if (node != null) {
            eval.setFeedback(AiJsonExtractor.text(node, "feedback", null));
            eval.setReferenceAnswer(AiJsonExtractor.text(node, "referenceAnswer", null));
            Integer score = AiJsonExtractor.intVal(node, "score", null);
            if (score != null) {
                eval.setScore(Math.max(0, Math.min(100, score)));
            }
        }
        if (eval.getFeedback() == null || eval.getFeedback().isBlank()) {
            eval.setFeedback("回答已收到，建议结合具体案例与量化结果进一步展开。");
        }
        if (eval.getReferenceAnswer() == null || eval.getReferenceAnswer().isBlank()) {
            eval.setReferenceAnswer("建议结合 STAR 法则或具体项目案例展开，突出关键技术与量化结果。");
        }
        if (eval.getScore() == null) {
            // 解析失败降级：按回答长度与关键词命中给保守分
            log.warn("[QwenAI] 面试评价 JSON 解析失败，降级为规则评分");
            int base = Math.min(85, 30 + answerText.trim().length() / 8);
            int bonus = 0;
            if (expectedKeywords != null && !expectedKeywords.isBlank()) {
                for (String kw : expectedKeywords.split(",")) {
                    String k = kw.trim();
                    if (!k.isEmpty() && answerText.contains(k)) {
                        bonus = Math.min(15, bonus + 3);
                    }
                }
            }
            eval.setScore(Math.min(100, base + bonus));
        }
        return eval;
    }

    @Override
    public AiReport generateReport(Integer type, Integer difficulty, String targetJob,
                                    List<InterviewQuestion> questions) {
        AiReport report = new AiReport();
        int total = questions == null ? 0 : questions.size();
        long answered = questions == null ? 0
                : questions.stream()
                        .filter(q -> q.getAnswerText() != null && !q.getAnswerText().isBlank())
                        .count();
        double completion = total == 0 ? 0 : (double) answered / total;

        // 先尝试真实模型报告
        if (total > 0) {
            StringBuilder qa = new StringBuilder();
            for (int i = 0; i < questions.size(); i++) {
                InterviewQuestion q = questions.get(i);
                qa.append("第").append(i + 1).append("题：").append(q.getQuestionText()).append('\n');
                qa.append("回答：").append(q.getAnswerText() == null || q.getAnswerText().isBlank()
                        ? "（未作答）" : q.getAnswerText()).append('\n');
            }
            String prompt = "你是一名资深面试官，请基于整场模拟面试生成评估报告。\n"
                    + "面试类型：" + (type == null ? "综合" : type == 0 ? "技术" : type == 1 ? "行为" : "综合")
                    + "；目标岗位：" + (targetJob == null || targetJob.isBlank() ? "通用" : targetJob) + "\n"
                    + "【问答记录】\n" + qa + "\n"
                    + "只输出 JSON 对象，格式：{\"overallScore\":0到100整数,"
                    + "\"dimensionScores\":{\"技术深度\":数字,\"表达清晰度\":数字,\"逻辑性\":数字,\"综合表现\":数字},"
                    + "\"summary\":\"总体评价\",\"suggestions\":\"改进建议\"}";
            JsonNode node = AiJsonExtractor.extractObject(aiChatService.chat(prompt));
            if (node != null) {
                Integer overall = AiJsonExtractor.intVal(node, "overallScore", null);
                if (overall != null) {
                    report.setOverallScore(Math.max(0, Math.min(100, overall)));
                    Map<String, Integer> dims = new HashMap<>();
                    JsonNode dimNode = node.get("dimensionScores");
                    if (dimNode != null && dimNode.isObject()) {
                        dimNode.fieldNames().forEachRemaining(f ->
                                dims.put(f, AiJsonExtractor.intVal(dimNode, f, null)));
                        dims.values().removeIf(v -> v == null);
                    }
                    report.setDimensionScores(dims.isEmpty() ? fallbackDims(report.getOverallScore()) : dims);
                    report.setSummary(AiJsonExtractor.text(node, "summary", null));
                    report.setSuggestions(AiJsonExtractor.text(node, "suggestions", null));
                    if (report.getSummary() == null || report.getSummary().isBlank()) {
                        report.setSummary(String.format("本次面试共%d题，完成%d题，综合得分%d分。",
                                total, answered, report.getOverallScore()));
                    }
                    if (report.getSuggestions() == null || report.getSuggestions().isBlank()) {
                        report.setSuggestions("建议针对薄弱维度做专项练习，回答中补充量化结果。");
                    }
                    return report;
                }
            }
            log.warn("[QwenAI] 面试报告 JSON 解析失败，降级为完成度评分");
        }

        // 降级：完成度评分（与 Mock 口径一致）
        int overall = (int) Math.round(60 + completion * 35);
        report.setOverallScore(overall);
        report.setDimensionScores(fallbackDims(overall));
        report.setSummary(String.format("本次面试共%d题，完成%d题，完成率%.0f%%，综合得分%d分。",
                total, answered, completion * 100, overall));
        report.setSuggestions(completion < 1.0
                ? "建议完成全部题目以获得更准确的评估；回答深度有待加强。"
                : "整体表现良好，可继续保持并挑战更高难度。");
        return report;
    }

    private Map<String, Integer> fallbackDims(int overall) {
        Map<String, Integer> dims = new HashMap<>();
        dims.put("技术深度", Math.max(0, overall - 5));
        dims.put("表达清晰度", overall);
        dims.put("逻辑性", Math.max(0, overall - 3));
        dims.put("综合表现", Math.min(100, overall + 2));
        return dims;
    }
}
