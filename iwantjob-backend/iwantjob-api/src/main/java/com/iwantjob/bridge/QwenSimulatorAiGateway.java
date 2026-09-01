package com.iwantjob.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwantjob.ai.AiChatService;
import com.iwantjob.simulator.ai.AiFeedback;
import com.iwantjob.simulator.ai.AiReport;
import com.iwantjob.simulator.ai.SimulatorAiGateway;
import com.iwantjob.simulator.entity.SimulatorChoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟舱 AI 网关千问真实实现（R1 桥接）。
 * <p>
 * 仅在 ai.qwen.enabled=true 时装配，内部包装 {@link AiChatService}。
 * 即时反馈与会话报告要求模型输出 JSON，解析失败时降级处理，
 * 保证业务链路（选择落库、事件触发）不中断。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "true")
public class QwenSimulatorAiGateway implements SimulatorAiGateway {

    private final AiChatService aiChatService;

    @Override
    public AiFeedback generateScenarioFeedback(String nodeDesc,
                                               String aiPromptSnippet,
                                               String optionText,
                                               String presetSoftTags) {
        AiFeedback fb = new AiFeedback();
        String tags = (presetSoftTags != null && !presetSoftTags.isBlank())
                ? presetSoftTags : "沟通协作";

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名职场软技能教练。用户正在做职业情境模拟演练。\n")
                .append("【情境】").append(nodeDesc == null ? "" : nodeDesc).append('\n');
        if (aiPromptSnippet != null && !aiPromptSnippet.isBlank()) {
            prompt.append("【情境补充】").append(aiPromptSnippet).append('\n');
        }
        prompt.append("【用户选择】").append(optionText == null ? "" : optionText).append('\n')
                .append("【该选择预置的软技能标签】").append(tags).append('\n')
                .append("请对用户的选择给出简短评价与建议（80字内）。\n")
                .append("只输出 JSON 对象，格式：{\"feedback\":\"评价\",\"softSkillTags\":\"标签1,标签2\"}");
        JsonNode node = AiJsonExtractor.extractObject(aiChatService.chat(prompt.toString()));
        if (node != null) {
            String feedback = AiJsonExtractor.text(node, "feedback", null);
            if (feedback != null && !feedback.isBlank()) {
                fb.setFeedback(feedback);
            }
            String modelTags = AiJsonExtractor.text(node, "softSkillTags", null);
            if (modelTags != null && !modelTags.isBlank()) {
                fb.setSoftSkillTags(modelTags);
            } else {
                fb.setSoftSkillTags(tags);
            }
        }
        if (fb.getFeedback() == null || fb.getFeedback().isBlank()) {
            log.warn("[QwenAI] 模拟舱反馈解析失败，降级为模板反馈");
            fb.setFeedback("选择体现了对职场情境的思考，可进一步考虑对他人与后续工作的影响。");
            fb.setSoftSkillTags(tags);
        }
        return fb;
    }

    @Override
    public AiReport generateReport(Integer scenarioType, Integer difficulty,
                                    List<SimulatorChoice> choices) {
        AiReport report = new AiReport();
        int total = choices == null ? 0 : choices.size();

        if (total > 0) {
            StringBuilder history = new StringBuilder();
            for (int i = 0; i < choices.size(); i++) {
                SimulatorChoice c = choices.get(i);
                history.append("第").append(i + 1).append("步 情境：")
                        .append(c.getNodeDesc() == null ? "" : c.getNodeDesc()).append('\n')
                        .append("选择：").append(c.getUserChoice() == null ? "" : c.getUserChoice()).append('\n');
            }
            String prompt = "你是一名职场软技能评估专家。以下是一次职业情境模拟演练的完整记录，"
                    + "请生成软技能评估报告。\n"
                    + "场景类型：" + scenarioTypeDesc(scenarioType) + "\n"
                    + "【演练记录】\n" + history + "\n"
                    + "只输出 JSON 对象，格式：{\"overallScore\":0到100整数,"
                    + "\"dimensionScores\":{\"沟通协作\":数字,\"应变能力\":数字,\"抗压\":数字,\"跨部门协作\":数字},"
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
                    report.setDimensionScores(dims.isEmpty()
                            ? fallbackDims(report.getOverallScore()) : dims);
                    report.setSummary(AiJsonExtractor.text(node, "summary", null));
                    report.setSuggestions(AiJsonExtractor.text(node, "suggestions", null));
                    if (report.getSummary() == null || report.getSummary().isBlank()) {
                        report.setSummary(String.format("本次演练共%d步，综合得分%d分。",
                                total, report.getOverallScore()));
                    }
                    if (report.getSuggestions() == null || report.getSuggestions().isBlank()) {
                        report.setSuggestions("建议针对薄弱维度做专项练习。");
                    }
                    return report;
                }
            }
            log.warn("[QwenAI] 模拟舱报告解析失败，降级为标签覆盖度评分");
        }

        // 降级：软技能标签覆盖度评分（与 Mock 口径一致）
        long tagged = choices == null ? 0
                : choices.stream()
                        .filter(c -> c.getSoftSkillTags() != null && !c.getSoftSkillTags().isBlank())
                        .count();
        double coverage = total == 0 ? 0 : (double) tagged / total;
        int overall = (int) Math.round(55 + coverage * 37);
        report.setOverallScore(overall);
        report.setDimensionScores(fallbackDims(overall));
        report.setSummary(String.format("本次%s场景演练共做出%d次选择，软技能标签覆盖%d次，综合得分%d分。",
                scenarioTypeDesc(scenarioType), total, tagged, overall));
        report.setSuggestions(overall < 75
                ? "可重点加强沟通表达与换位思考，遇到冲突时先共情再提出方案。"
                : "整体软技能表现良好，可挑战更高难度或更复杂场景。");
        return report;
    }

    private String scenarioTypeDesc(Integer scenarioType) {
        if (scenarioType == null) {
            return "综合";
        }
        return switch (scenarioType) {
            case 0 -> "入职";
            case 1 -> "向上汇报";
            case 2 -> "冲突处理";
            case 3 -> "跨部门协作";
            default -> "综合";
        };
    }

    private Map<String, Integer> fallbackDims(int overall) {
        Map<String, Integer> dims = new HashMap<>();
        dims.put("沟通协作", overall);
        dims.put("应变能力", Math.max(0, overall - 4));
        dims.put("抗压", Math.max(0, overall - 2));
        dims.put("跨部门协作", Math.min(100, overall - 1));
        return dims;
    }
}
