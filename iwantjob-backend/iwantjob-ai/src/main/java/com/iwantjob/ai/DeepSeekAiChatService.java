package com.iwantjob.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenAI 兼容协议真实实现（当前接入阿里云百炼 MaaS 上的 deepseek-v4-flash）。
 * 仅在 ai.qwen.enabled=true 时装配，否则走 {@link MockAiChatService}。
 * <p>
 * 职责：
 * <ul>
 *   <li>封装 OpenAI 兼容 /chat/completions 的同步 / SSE 流式调用（HTTP 层用 Hutool）</li>
 *   <li>重试：最多 3 次，指数退避 1s / 2s / 4s（仅同步接口重试）</li>
 *   <li>限流：Redis 令牌桶，每用户每分钟 10 次（key=ai:rate:{userId}）；
 *       获取不到 userId 时跳过限流</li>
 * </ul>
 * <p>
 * 接口约定（相对 ai.qwen.base-url）：
 * <ul>
 *   <li>同步：POST {base-url}/chat/completions，stream=false</li>
 *   <li>流式：POST {base-url}/chat/completions，stream=true，按 SSE 解析 data: 行</li>
 * </ul>
 * deepseek-v4-flash 为推理模型：增量里除 content 外还会携带 reasoning_content（思考过程），
 * 这里只向业务层透传 content，reasoning 静默丢弃。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "true")
public class DeepSeekAiChatService implements AiChatService {

    private final AiProperties aiProperties;
    private final StringRedisTemplate redisTemplate;

    /** 每用户每分钟允许调用次数 */
    private static final int RATE_PER_MINUTE = 10;
    /** 令牌桶容量，允许短时突发到 10 次 */
    private static final int CAPACITY = 10;
    /** 令牌桶 key TTL（秒） */
    private static final int RATE_KEY_TTL_SECONDS = 120;

    /** 最大重试次数（含首次） */
    private static final int MAX_RETRY = 3;
    /** 指数退避间隔（毫秒）：1s / 2s / 4s */
    private static final long[] BACKOFF_MS = {1000L, 2000L, 4000L};

    /** 推理模型思考需要更长生成上限，避免回答被 max_tokens 截断 */
    private static final int MAX_TOKENS = 8192;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        // tokens/last_time 存于 hash；rate 为每秒令牌补充速率，capacity 为桶上限
        RATE_LIMIT_SCRIPT.setScriptText(
                "local key = KEYS[1] " +
                "local rate = tonumber(ARGV[1]) " +
                "local capacity = tonumber(ARGV[2]) " +
                "local now = tonumber(ARGV[3]) " +
                "local ttl = tonumber(ARGV[4]) " +
                "if redis.call('EXISTS', key) == 1 then " +
                "  redis.call('HSET', key, 'last_time', now) " +
                "else " +
                "  redis.call('HMSET', key, 'tokens', capacity, 'last_time', now) " +
                "  redis.call('EXPIRE', key, ttl) " +
                "end " +
                "local tokens = tonumber(redis.call('HGET', key, 'tokens')) " +
                "local last_time = tonumber(redis.call('HGET', key, 'last_time')) " +
                "local delta = math.max(0, now - last_time) / 1000.0 * rate " +
                "tokens = math.min(capacity, tokens + delta) " +
                "if tokens < 1 then " +
                "  return 0 " +
                "else " +
                "  redis.call('HSET', key, 'tokens', tokens - 1) " +
                "  return 1 " +
                "end"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    @Override
    public String chat(String prompt) {
        return chat(prompt, Collections.emptyList());
    }

    @Override
    public String chat(String prompt, List<ChatMessage> history) {
        checkRateLimit();
        String url = chatCompletionsUrl();
        JSONObject body = buildRequestBody(prompt, history, false);
        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try (HttpResponse resp = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey())
                    .header("Content-Type", "application/json")
                    .timeout(120_000)
                    .body(body.toString())
                    .execute()) {
                if (!resp.isOk()) {
                    throw new IllegalStateException("HTTP " + resp.getStatus()
                            + " body=" + truncate(resp.body(), 500));
                }
                String content = extractContent(resp.body());
                if (content == null || content.isEmpty()) {
                    // 推理模型偶发只吐 reasoning 不吐正文，按可重试错误处理
                    throw new IllegalStateException("响应 content 为空 body=" + truncate(resp.body(), 500));
                }
                return content;
            } catch (Exception e) {
                lastError = e;
                log.warn("AI 同步调用失败 attempt={}/{} err={}", attempt, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY) {
                    sleepBackoff(BACKOFF_MS[attempt - 1]);
                }
            }
        }
        throw new RuntimeException("AI 调用重试 " + MAX_RETRY + " 次仍失败", lastError);
    }

    @Override
    public void chatStream(String prompt, List<ChatMessage> history, StreamCallback callback) {
        try {
            checkRateLimit();
            String url = chatCompletionsUrl();
            JSONObject body = buildRequestBody(prompt, history, true);
            StringBuilder full = new StringBuilder();
            try (HttpResponse resp = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .timeout(180_000)
                    .body(body.toString())
                    .executeAsync()) {
                if (!resp.isOk()) {
                    throw new IllegalStateException("HTTP " + resp.getStatus()
                            + " body=" + truncate(resp.body(), 500));
                }
                try (InputStream in = resp.bodyStream();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty() || !line.startsWith("data:")) {
                            continue;
                        }
                        String data = line.substring("data:".length()).trim();
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        String delta = extractDeltaContent(data);
                        if (delta != null && !delta.isEmpty()) {
                            full.append(delta);
                            callback.onMessage(delta);
                        }
                    }
                }
            }
            callback.onComplete(full.toString());
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    /** 拼接 {base-url}/chat/completions，兼容 base-url 带不带尾部斜杠 */
    private String chatCompletionsUrl() {
        String base = aiProperties.getBaseUrl();
        if (base == null || base.isBlank()) {
            throw new IllegalStateException("ai.qwen.base-url 未配置");
        }
        return (base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + "/chat/completions";
    }

    private String apiKey() {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new IllegalStateException(
                    "ai.qwen.api-key 未配置，无法启用真实 AI 调用；请检查环境变量 QWEN_API_KEY");
        }
        return aiProperties.getApiKey();
    }

    private JSONObject buildRequestBody(String prompt, List<ChatMessage> history, boolean stream) {
        JSONArray messages = new JSONArray();
        if (history != null && !history.isEmpty()) {
            for (ChatMessage cm : history) {
                if (cm == null || cm.getContent() == null) {
                    continue;
                }
                messages.add(new JSONObject()
                        .set("role", normalizeRole(cm.getRole()))
                        .set("content", cm.getContent()));
            }
        }
        messages.add(new JSONObject()
                .set("role", "user")
                .set("content", prompt == null ? "" : prompt));
        return new JSONObject()
                .set("model", aiProperties.getModel())
                .set("messages", messages)
                .set("max_tokens", MAX_TOKENS)
                .set("stream", stream);
    }

    /** 兜底归一化为 OpenAI 的三种合法角色 */
    private String normalizeRole(String role) {
        if ("system".equals(role) || "assistant".equals(role) || "user".equals(role)) {
            return role;
        }
        return "user";
    }

    /** 同步响应：取 choices[0].message.content */
    private String extractContent(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "";
        }
        JSONObject root = JSONUtil.parseObj(responseBody);
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            return "";
        }
        String content = message.getStr("content");
        return content == null ? "" : content;
    }

    /** SSE 增量：取 choices[0].delta.content（reasoning_content 丢弃） */
    private String extractDeltaContent(String data) {
        try {
            JSONObject root = JSONUtil.parseObj(data);
            JSONArray choices = root.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
            if (delta == null) {
                return "";
            }
            String content = delta.getStr("content");
            return content == null ? "" : content;
        } catch (Exception e) {
            // 单个 chunk 解析失败不终止整条流
            log.warn("SSE chunk 解析失败，已跳过: {}", truncate(data, 200));
            return "";
        }
    }

    /**
     * 按用户限流：每用户每分钟 10 次；取不到 userId 则跳过
     */
    private void checkRateLimit() {
        Long userId = AiUserContext.getCurrentUserId();
        if (userId == null) {
            log.debug("未取到 userId，跳过 AI 限流");
            return;
        }
        String key = "ai:rate:" + userId;
        double ratePerSec = RATE_PER_MINUTE / 60.0;
        Long allowed = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(ratePerSec),
                String.valueOf(CAPACITY),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(RATE_KEY_TTL_SECONDS)
        );
        if (allowed == null || allowed == 0L) {
            log.warn("AI 限流触发 userId={}", userId);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private void sleepBackoff(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI 调用重试等待被中断", e);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
