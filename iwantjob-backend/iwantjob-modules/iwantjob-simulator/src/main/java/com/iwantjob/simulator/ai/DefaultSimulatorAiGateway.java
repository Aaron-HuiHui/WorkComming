package com.iwantjob.simulator.ai;

import com.iwantjob.simulator.entity.SimulatorChoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟舱 AI 网关默认实现：返回模拟数据。
 * <p>
 * 不依赖任何外部大模型，保证模块可独立编译与本地演示。
 * 仅在 ai.qwen.enabled=false（或缺失）时装配；
 * ai.qwen.enabled=true 时由 api 聚合模块的 QwenSimulatorAiGateway 接管。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultSimulatorAiGateway implements SimulatorAiGateway {

    @Override
    public AiFeedback generateScenarioFeedback(String nodeDesc,
                                               String aiPromptSnippet,
                                               String optionText,
                                               String presetSoftTags) {
        log.debug("【Mock-AI】生成即时反馈: nodeDescLen={}, optionLen={}",
                nodeDesc == null ? 0 : nodeDesc.length(),
                optionText == null ? 0 : optionText.length());
        AiFeedback fb = new AiFeedback();

        // 软技能标签优先沿用选项预置，缺失时给默认值
        String tags = (presetSoftTags != null && !presetSoftTags.isBlank())
                ? presetSoftTags
                : "沟通协作";
        fb.setSoftSkillTags(tags);

        // 基于选项文本长度生成占位反馈
        int len = optionText == null ? 0 : optionText.trim().length();
        if (len == 0) {
            fb.setFeedback("选择内容为空，建议结合情境再思考一下。本次选择触发了软技能标签：" + tags + "。");
        } else if (len < 10) {
            fb.setFeedback("选择较简短，建议补充理由与预期效果。本次选择触发了软技能标签：" + tags + "。");
        } else {
            fb.setFeedback("选择体现了对职场情境的思考，可进一步考虑对他人与后续工作的影响。本次选择触发了软技能标签：" + tags + "。");
        }
        return fb;
    }

    @Override
    public AiReport generateReport(Integer scenarioType,
                                   Integer difficulty,
                                   List<SimulatorChoice> choices) {
        log.debug("【Mock-AI】生成报告: scenarioType={}, choiceCount={}",
                scenarioType, choices == null ? 0 : choices.size());
        AiReport report = new AiReport();

        int total = choices == null ? 0 : choices.size();
        // 完成度越高、有反馈与软技能标签越多，得分越高（55 ~ 92）
        long tagged = choices == null ? 0
                : choices.stream()
                        .filter(c -> c.getSoftSkillTags() != null && !c.getSoftSkillTags().isBlank())
                        .count();
        double coverage = total == 0 ? 0 : (double) tagged / total;
        int overall = (int) Math.round(55 + coverage * 37);

        Map<String, Integer> dims = new HashMap<>();
        dims.put("沟通协作", overall);
        dims.put("应变能力", Math.max(0, overall - 4));
        dims.put("抗压", Math.max(0, overall - 2));
        dims.put("跨部门协作", Math.min(100, overall - 1));
        report.setDimensionScores(dims);
        report.setOverallScore(overall);

        String typeDesc = scenarioType == null ? "综合"
                : switch (scenarioType) {
            case 0 -> "入职";
            case 1 -> "向上汇报";
            case 2 -> "冲突处理";
            case 3 -> "跨部门协作";
            default -> "综合";
        };
        report.setSummary(String.format(
                "本次%s场景演练共做出%d次选择，软技能标签覆盖%d次，综合得分%d分。",
                typeDesc, total, tagged, overall));

        StringBuilder sug = new StringBuilder();
        if (total < 3) {
            sug.append("演练步数偏少，建议完整走完分支以获得更准确评估；");
        }
        if (overall < 75) {
            sug.append("可重点加强沟通表达与换位思考，遇到冲突时先共情再提出方案；");
        } else {
            sug.append("整体软技能表现良好，可挑战更高难度或更复杂场景；");
        }
        sug.append("建议针对薄弱维度做专项练习。");
        report.setSuggestions(sug.toString());

        return report;
    }
}
