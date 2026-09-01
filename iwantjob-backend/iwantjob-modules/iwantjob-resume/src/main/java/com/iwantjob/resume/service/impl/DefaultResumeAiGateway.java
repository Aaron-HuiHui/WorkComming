package com.iwantjob.resume.service.impl;

import com.iwantjob.resume.service.ResumeAiGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 简历 AI 网关默认实现（模拟）
 * <p>
 * 不依赖 iwantjob-ai 模块，保证 resume 模块可独立编译运行。
 * 仅在 ai.qwen.enabled=false（或缺失）时装配；
 * ai.qwen.enabled=true 时由 api 聚合模块的 QwenResumeAiGateway 接管。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultResumeAiGateway implements ResumeAiGateway {

    @Override
    public String optimize(String content, Integer type, String targetLang) {
        log.info("[MockAI] 简历优化: type={}, targetLang={}, contentLen={}", type, targetLang, content == null ? 0 : content.length());
        if (content == null || content.isBlank()) {
            return "";
        }
        String typeLabel = switch (type == null ? 0 : type) {
            case 1 -> "翻译";
            case 2 -> "强化";
            default -> "润色";
        };
        // 模拟：在原文基础上加一段前缀标记，便于联调可识别
        return String.format(
                "[AI%s优化-Mock] %s%s",
                typeLabel,
                (targetLang != null && !targetLang.isBlank()) ? ("->" + targetLang + " ") : "",
                content
        );
    }

    @Override
    public Integer score(String content) {
        log.info("[MockAI] 简历评分: contentLen={}", content == null ? 0 : content.length());
        if (content == null || content.isBlank()) {
            return 40;
        }
        // 模拟评分：基于文本长度给一个 60-90 之间的分数，长度越长分数越高（封顶 90）
        int len = content.length();
        int base = 60;
        int bonus = Math.min(30, len / 50);
        return Math.min(90, base + bonus);
    }
}
