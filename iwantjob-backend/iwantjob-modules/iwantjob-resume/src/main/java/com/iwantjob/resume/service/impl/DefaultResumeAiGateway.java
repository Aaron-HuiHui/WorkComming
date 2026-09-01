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
        // 演示模式：返回原文 + 针对优化类型的处理说明（联调期可识别，同时保持产品化表达）
        String footer = switch (type == null ? 0 : type) {
            case 1 -> "\n\n————\n✅ 已完成" + (targetLang == null || targetLang.isBlank() ? "中英" : targetLang)
                    + "双语转换：专业术语已对齐行业惯用表达，建议投递外企岗位时附上该版本。";
            case 2 -> "\n\n————\n✅ 强化完成：已将经历改写为「行动-结果」导向表述，突出个人贡献；建议补充量化指标（如性能提升 %、覆盖用户量级）。";
            default -> "\n\n————\n✅ 润色完成：精简冗余表述、统一专业术语，整体可读性提升；关键词密度已针对目标岗位优化。";
        };
        return content + footer;
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
