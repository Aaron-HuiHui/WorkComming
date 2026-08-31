package com.iwantjob.simulator.ai;

import com.iwantjob.simulator.entity.SimulatorChoice;

import java.util.List;

/**
 * 模拟舱 AI 网关：封装职业模拟场景的 AI 能力（即时反馈 / 会话报告）。
 * <p>
 * 默认实现 {@link DefaultSimulatorAiGateway} 返回模拟数据，便于脱离千问配额独立开发与演示；
 * 接入真实大模型时仅需提供新的实现并注入即可，业务层无需改动。
 * </p>
 * <p>
 * Prompt 规范（参考开发文档 7.2 节）：
 * <ul>
 *   <li>系统提示词固定角色设定 + "不得跳出职场场景"约束 + 输出格式（情境描述 + 选项 JSON）</li>
 *   <li>反馈生成：基于 node_desc + ai_prompt_snippet + 用户选择，输出评价与软技能标签</li>
 *   <li>报告生成：基于全程 choices，输出软技能评分（沟通协作/应变能力/抗压/跨部门协作）</li>
 * </ul>
 */
public interface SimulatorAiGateway {

    /**
     * 生成单次选择的即时反馈
     *
     * @param nodeDesc         节点情境描述
     * @param aiPromptSnippet  注入大模型的场景片段提示
     * @param optionText       用户选择的选项文本
     * @param presetSoftTags   选项预置的软技能标签（可空）
     * @return AI 反馈（评价 + 软技能标签）
     */
    AiFeedback generateScenarioFeedback(String nodeDesc,
                                        String aiPromptSnippet,
                                        String optionText,
                                        String presetSoftTags);

    /**
     * 基于全程 choices 生成软技能评分报告
     *
     * @param scenarioType  场景类型 ScenarioTypeEnum: 0-入职,1-向上汇报,2-冲突处理,3-跨部门协作
     * @param difficulty    难度
     * @param choices       全部选择记录（按时间升序）
     * @return AI 报告（综合得分 + 各维度评分 + 评价 + 建议）
     */
    AiReport generateReport(Integer scenarioType,
                            Integer difficulty,
                            List<SimulatorChoice> choices);
}
