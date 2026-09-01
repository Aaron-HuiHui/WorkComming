package com.iwantjob.bridge;

import com.iwantjob.ai.AiChatService;
import com.iwantjob.simulator.ai.AiFeedback;
import com.iwantjob.simulator.ai.AiReport;
import com.iwantjob.simulator.entity.SimulatorChoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 模拟舱千问网关单元测试（R1 桥接）：
 * JSON 正常解析 / markdown 包裹解析 / 非 JSON 输出降级 / 空选择降级
 */
@ExtendWith(MockitoExtension.class)
class QwenSimulatorAiGatewayTest {

    @Mock
    private AiChatService aiChatService;

    private QwenSimulatorAiGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new QwenSimulatorAiGateway(aiChatService);
    }

    // ==================== 即时反馈 ====================

    @Test
    void feedbackWithPlainJsonShouldParseFields() {
        when(aiChatService.chat(anyString())).thenReturn(
                "{\"feedback\":\"很好的选择\",\"softSkillTags\":\"沟通协作,换位思考\"}");
        AiFeedback fb = gateway.generateScenarioFeedback(
                "入职第一周情境", null, "接受邀约", "沟通协作");
        assertEquals("很好的选择", fb.getFeedback());
        assertEquals("沟通协作,换位思考", fb.getSoftSkillTags());
    }

    @Test
    void feedbackWrappedInMarkdownCodeBlockShouldStillParse() {
        when(aiChatService.chat(anyString())).thenReturn(
                "好的，以下是评价：\n```json\n{\"feedback\":\"建议补充理由\",\"softSkillTags\":\"专注执行\"}\n```");
        AiFeedback fb = gateway.generateScenarioFeedback(
                "情境", "补充提示", "婉拒邀约", "专注执行");
        assertEquals("建议补充理由", fb.getFeedback());
        assertEquals("专注执行", fb.getSoftSkillTags());
    }

    @Test
    void feedbackWithNonJsonOutputShouldFallbackToTemplate() {
        when(aiChatService.chat(anyString())).thenReturn("这不是 JSON 格式的自由文本回复");
        AiFeedback fb = gateway.generateScenarioFeedback(
                "情境", null, "某个选择", "抗压");
        assertNotNull(fb.getFeedback());
        assertTrue(fb.getFeedback().contains("思考"), "降级反馈应包含模板文案");
        assertEquals("抗压", fb.getSoftSkillTags());
    }

    @Test
    void feedbackWithNullPresetTagsShouldUseDefaultTag() {
        when(aiChatService.chat(anyString())).thenReturn("无法解析的输出");
        AiFeedback fb = gateway.generateScenarioFeedback(
                "情境", null, "选择", null);
        assertEquals("沟通协作", fb.getSoftSkillTags());
    }

    @Test
    void feedbackWithJsonButMissingTagsShouldKeepPresetTags() {
        when(aiChatService.chat(anyString())).thenReturn("{\"feedback\":\"只有反馈没有标签\"}");
        AiFeedback fb = gateway.generateScenarioFeedback(
                "情境", null, "选择", "预置标签");
        assertEquals("只有反馈没有标签", fb.getFeedback());
        assertEquals("预置标签", fb.getSoftSkillTags());
    }

    // ==================== 会话报告 ====================

    @Test
    void reportWithEmptyChoicesShouldFallbackToBaseScore() {
        AiReport report = gateway.generateReport(0, 1, new ArrayList<>());
        assertEquals(55, report.getOverallScore());
        assertNotNull(report.getSummary());
        assertNotNull(report.getSuggestions());
        assertEquals(4, report.getDimensionScores().size());
    }

    @Test
    void reportWithNullChoicesShouldFallbackToBaseScore() {
        AiReport report = gateway.generateReport(0, 1, null);
        assertEquals(55, report.getOverallScore());
    }

    @Test
    void reportWithValidJsonShouldParseModelResult() {
        when(aiChatService.chat(anyString())).thenReturn(
                "{\"overallScore\":88,"
                        + "\"dimensionScores\":{\"沟通协作\":90,\"应变能力\":85,\"抗压\":80,\"跨部门协作\":87},"
                        + "\"summary\":\"整体表现优秀\",\"suggestions\":\"继续挑战高难度场景\"}");
        List<SimulatorChoice> choices = new ArrayList<>();
        SimulatorChoice c = new SimulatorChoice();
        c.setNodeDesc("情境");
        c.setUserChoice("选择");
        c.setSoftSkillTags("沟通协作");
        choices.add(c);

        AiReport report = gateway.generateReport(1, 2, choices);

        assertEquals(88, report.getOverallScore());
        assertEquals("整体表现优秀", report.getSummary());
        assertEquals("继续挑战高难度场景", report.getSuggestions());
        assertEquals(90, report.getDimensionScores().get("沟通协作"));
    }

    @Test
    void reportScoreShouldBeClampedToValidRange() {
        when(aiChatService.chat(anyString())).thenReturn(
                "{\"overallScore\":150,\"summary\":\"越界分数\",\"suggestions\":\"无\"}");
        List<SimulatorChoice> choices = new ArrayList<>();
        SimulatorChoice c = new SimulatorChoice();
        c.setNodeDesc("情境");
        c.setUserChoice("选择");
        c.setSoftSkillTags("沟通");
        choices.add(c);

        AiReport report = gateway.generateReport(0, 1, choices);
        assertEquals(100, report.getOverallScore());
    }

    @Test
    void reportWithInvalidJsonShouldFallbackToCoverageScore() {
        when(aiChatService.chat(anyString())).thenReturn("模型返回了非JSON内容");
        List<SimulatorChoice> choices = new ArrayList<>();
        SimulatorChoice c = new SimulatorChoice();
        c.setNodeDesc("情境");
        c.setUserChoice("选择");
        c.setSoftSkillTags("沟通协作");
        choices.add(c);

        AiReport report = gateway.generateReport(0, 1, choices);
        // 1 步选择、标签覆盖 100% → 55 + 37 = 92
        assertEquals(92, report.getOverallScore());
    }
}
