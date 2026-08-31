package com.iwantjob.resume.service;

/**
 * 简历 AI 网关接口
 * <p>
 * 设计动机：resume 模块的 pom 当前未直接依赖 iwantjob-ai 模块，
 * 为保证 resume 模块可独立编译，这里定义一个本模块的抽象接口，
 * 真实 AI 桥接实现（包装 AiChatService）由 api 聚合模块或 config 补充，
 * 默认实现 {@link com.iwantjob.resume.service.impl.DefaultResumeAiGateway}
 * 提供模拟返回，便于联调与单测。
 */
public interface ResumeAiGateway {

    /**
     * AI 简历优化
     *
     * @param content    原文（content_json 摘要或整段文本）
     * @param type       优化类型：0-润色,1-翻译,2-强化
     * @param targetLang 目标语言（type=1 翻译时使用，可空）
     * @return 优化后文本
     */
    String optimize(String content, Integer type, String targetLang);

    /**
     * AI 简历评分
     *
     * @param content 简历内容文本
     * @return 评分（0-100）
     */
    Integer score(String content);
}
