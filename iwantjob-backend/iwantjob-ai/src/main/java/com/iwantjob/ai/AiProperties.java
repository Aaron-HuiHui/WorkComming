package com.iwantjob.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 千问配置，绑定 ai.qwen.* 前缀
 * <pre>
 * ai:
 *   qwen:
 *     api-key: ${QWEN_API_KEY:}
 *     model: qwen-plus
 *     enabled: false
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.qwen")
public class AiProperties {

    /** 千问 API Key，建议通过环境变量 QWEN_API_KEY 注入 */
    private String apiKey;

    /** 模型名，默认 qwen-plus */
    private String model;

    /** 是否启用真实千问调用，默认 false 使用 Mock */
    private boolean enabled;
}
