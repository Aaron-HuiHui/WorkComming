package com.iwantjob.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模型配置，绑定 ai.qwen.* 前缀（历史命名，当前接入 deepseek-v4-flash）。
 * <pre>
 * ai:
 *   qwen:
 *     api-key: ${QWEN_API_KEY:}
 *     base-url: https://.../compatible-mode/v1
 *     model: deepseek-v4-flash
 *     enabled: false
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.qwen")
public class AiProperties {

    /** API Key，建议通过环境变量 QWEN_API_KEY 注入 */
    private String apiKey;

    /** OpenAI 兼容地址（形如 https://host/compatible-mode/v1） */
    private String baseUrl;

    /** 模型名，默认 deepseek-v4-flash */
    private String model;

    /** 是否启用真实模型调用，默认 false 使用 Mock */
    private boolean enabled;
}
