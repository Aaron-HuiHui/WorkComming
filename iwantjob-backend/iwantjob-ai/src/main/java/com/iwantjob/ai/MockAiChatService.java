package com.iwantjob.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Mock 实现，开发阶段无需真实千问 API Key 即可联调。
 * 仅在 ai.qwen.enabled=false（或缺失）时装配。
 * <p>
 * 根据 prompt 关键词返回固定响应：
 * <ul>
 *   <li>"简历" → 简历优化模拟建议</li>
 *   <li>"面试" → 面试反馈模拟</li>
 *   <li>"模拟舱"/"场景" → 场景描述 + 2~3 个选项 JSON</li>
 *   <li>其他 → 通用 AI 回复</li>
 * </ul>
 * 流式按 50ms/字符模拟逐字输出。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "false", matchIfMissing = true)
public class MockAiChatService implements AiChatService {

    private static final long STREAM_INTERVAL_MS = 50L;

    @Override
    public String chat(String prompt) {
        return chat(prompt, Collections.emptyList());
    }

    @Override
    public String chat(String prompt, List<ChatMessage> history) {
        log.info("[Mock-AI] 收到请求 prompt={}, historySize={}",
                summarize(prompt),
                history == null ? 0 : history.size());
        String resp = mockResponse(prompt);
        log.info("[Mock-AI] 返回响应 len={}", resp.length());
        return resp;
    }

    @Override
    public void chatStream(String prompt, List<ChatMessage> history, StreamCallback callback) {
        log.info("[Mock-AI] 流式响应 prompt={}", summarize(prompt));
        String full = mockResponse(prompt);
        try {
            for (int i = 0; i < full.length(); i++) {
                callback.onMessage(String.valueOf(full.charAt(i)));
                try {
                    Thread.sleep(STREAM_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    callback.onError(e);
                    return;
                }
            }
            callback.onComplete(full);
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private String mockResponse(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return "[Mock-通用回复] 收到空 prompt，这是 Mock 模式回复。";
        }
        // 关键词命中
        if (prompt.contains("简历")) {
            return "[Mock-简历优化] 1) 量化经历：把'负责 X'改为'通过 Y 方法达成 Z% 提升'。\n"
                    + "2) 关键词匹配：将 JD 中的高频词融入技能栏，提升 ATS 命中率。\n"
                    + "3) 结构调整：实习经历前置教育之后；项目按 STAR 法重写。\n"
                    + "4) 排版：单页优先，去除冗余自我评价。";
        }
        if (prompt.contains("面试")) {
            return "[Mock-面试反馈] 答题结构较清晰，但缺少量化结果与权衡分析。\n"
                    + "建议：1) 用 STAR 法补充情境数据；2) 技术题加权衡（为什么不用方案 B）；"
                    + "3) 反问环节体现对岗位的理解。";
        }
        if (prompt.contains("模拟舱") || prompt.contains("场景")) {
            return "{\"scene\":\"你作为新员工参加入职第一次站会，组长请你同步上周产出。\","
                    + "\"options\":["
                    + "\"A. 主动汇报已完成项与本周计划\","
                    + "\"B. 等组长点名再回答\","
                    + "\"C. 反问本周优先级再决定是否汇报\""
                    + "]}";
        }
        return "[Mock-通用回复] 你说的是：'" + summarize(prompt) + "'。\n"
                + "这是 Mock 模式回复；将 ai.qwen.enabled 设为 true 并配置 QWEN_API_KEY 后即可切换至真实千问。";
    }

    private String summarize(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.length() <= 40 ? prompt : prompt.substring(0, 40) + "...";
    }
}
