package com.iwantjob.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwantjob.ai.AiChatService;
import com.iwantjob.resume.service.ResumeAiGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 简历 AI 网关千问真实实现（R1 桥接）。
 * <p>
 * 仅在 ai.qwen.enabled=true 时装配，内部包装 {@link AiChatService}（此时为 QwenAiChatService）。
 * Mock 模式下由 resume 模块的 DefaultResumeAiGateway 提供。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "true")
public class QwenResumeAiGateway implements ResumeAiGateway {

    private final AiChatService aiChatService;

    @Override
    public String optimize(String content, Integer type, String targetLang) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String typeLabel = switch (type == null ? 0 : type) {
            case 1 -> "翻译";
            case 2 -> "强化";
            default -> "润色";
        };
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名资深简历顾问。请对以下简历内容进行").append(typeLabel).append("处理。");
        if ("翻译".equals(typeLabel) && targetLang != null && !targetLang.isBlank()) {
            prompt.append("目标语言：").append(targetLang).append("。");
        }
        if ("强化".equals(typeLabel)) {
            prompt.append("要求量化成果、突出与岗位的匹配度。");
        }
        prompt.append("只输出处理后的简历正文，不要任何解释、前言或代码块标记。\n\n【简历内容】\n")
                .append(content);
        String reply = aiChatService.chat(prompt.toString());
        log.info("[QwenAI] 简历{}完成, 原文len={}, 输出len={}", typeLabel,
                content.length(), reply == null ? 0 : reply.length());
        return reply == null ? "" : reply.trim();
    }

    @Override
    public Integer score(String content) {
        if (content == null || content.isBlank()) {
            return 40;
        }
        String prompt = "你是一名简历评估专家。请对以下简历按百分制打分（0-100），"
                + "评估维度：结构完整性、内容量化程度、岗位匹配度、语言专业度。\n"
                + "只输出一个 JSON 对象，格式：{\"score\": 数字, \"comment\": \"一句话点评\"}\n\n"
                + "【简历内容】\n" + content;
        JsonNode node = AiJsonExtractor.extractObject(aiChatService.chat(prompt));
        Integer score = AiJsonExtractor.intVal(node, "score", null);
        if (score != null) {
            return Math.max(0, Math.min(100, score));
        }
        // 解析失败降级：按文本长度给保守分
        log.warn("[QwenAI] 简历评分 JSON 解析失败，降级为长度评分");
        return Math.min(90, 60 + content.length() / 50);
    }
}
