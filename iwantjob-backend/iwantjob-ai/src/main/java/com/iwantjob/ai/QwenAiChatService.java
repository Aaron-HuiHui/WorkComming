package com.iwantjob.ai;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 千问真实实现。
 * 仅在 ai.qwen.enabled=true 时装配，否则走 {@link MockAiChatService}。
 * <p>
 * 职责：
 * <ul>
 *   <li>封装千问 SDK 同步 / 流式调用</li>
 *   <li>重试：最多 3 次，指数退避 1s / 2s / 4s</li>
 *   <li>限流：Redis 令牌桶，每用户每分钟 10 次（key=ai:rate:{userId}）；
 *       获取不到 userId 时跳过限流</li>
 * </ul>
 * <p>
 * SDK 版本：dashscope-sdk-java 2.16.4
 * <ul>
 *   <li>同步：{@code generation.call(param)} 返回 {@link GenerationResult}</li>
 *   <li>流式回调：{@code generation.call(param, ResultCallback)}，回调方法为
 *       {@code onEvent / onComplete / onError(Exception)}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.qwen.enabled", havingValue = "true")
public class QwenAiChatService implements AiChatService {

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

    private volatile Generation generation;

    private Generation getGeneration() {
        Generation g = generation;
        if (g != null) {
            return g;
        }
        synchronized (this) {
            if (generation == null) {
                if (!StringUtils.hasText(aiProperties.getApiKey())) {
                    throw new IllegalStateException(
                            "ai.qwen.api-key 未配置，无法启用真实千问调用；请检查 QWEN_API_KEY 环境变量");
                }
                // 无参构造不依赖 dashscope.properties，apiKey 通过 GenerationParam 传入
                generation = new Generation();
            }
            return generation;
        }
    }

    @Override
    public String chat(String prompt) {
        return chat(prompt, Collections.emptyList());
    }

    @Override
    public String chat(String prompt, List<ChatMessage> history) {
        checkRateLimit();
        GenerationParam param = buildParam(prompt, history, false);
        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                GenerationResult result = getGeneration().call(param);
                return extractContent(result);
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                // SDK checked 异常，可重试
                lastError = e;
                log.warn("千问同步调用失败 attempt={}/{} err={}", attempt, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY) {
                    sleepBackoff(BACKOFF_MS[attempt - 1]);
                }
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("千问同步调用运行时异常 attempt={}/{} err={}", attempt, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY) {
                    sleepBackoff(BACKOFF_MS[attempt - 1]);
                }
            }
        }
        throw new RuntimeException("千问调用重试 " + MAX_RETRY + " 次仍失败", lastError);
    }

    @Override
    public void chatStream(String prompt, List<ChatMessage> history, StreamCallback callback) {
        try {
            checkRateLimit();
            GenerationParam param = buildParam(prompt, history, true);
            StringBuilder full = new StringBuilder();
            // call(param, ResultCallback) 支持流式回调：onEvent 收到每个增量、onComplete 结束、onError 异常
            getGeneration().call(param, new ResultCallback<GenerationResult>() {
                @Override
                public void onEvent(GenerationResult message) {
                    String delta = extractContent(message);
                    if (delta != null && !delta.isEmpty()) {
                        full.append(delta);
                        callback.onMessage(delta);
                    }
                }

                @Override
                public void onComplete() {
                    callback.onComplete(full.toString());
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private GenerationParam buildParam(String prompt, List<ChatMessage> history, boolean stream) {
        List<Message> messages = new ArrayList<>();
        if (history != null && !history.isEmpty()) {
            for (ChatMessage cm : history) {
                Message m = toSdkMessage(cm);
                if (m != null) {
                    messages.add(m);
                }
            }
        }
        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build());
        GenerationParam param = GenerationParam.builder()
                .model(aiProperties.getModel())
                .apiKey(aiProperties.getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        if (stream) {
            // 增量输出：每次 onEvent 只返回 delta 而非累积全文
            param.setIncrementalOutput(true);
        }
        return param;
    }

    private Message toSdkMessage(ChatMessage cm) {
        if (cm == null || cm.getContent() == null) {
            return null;
        }
        String role = cm.getRole();
        String targetRole;
        if (Role.SYSTEM.getValue().equals(role)) {
            targetRole = Role.SYSTEM.getValue();
        } else if (Role.ASSISTANT.getValue().equals(role)) {
            targetRole = Role.ASSISTANT.getValue();
        } else if (Role.USER.getValue().equals(role)) {
            targetRole = Role.USER.getValue();
        } else {
            // 兜底按 user
            targetRole = Role.USER.getValue();
        }
        return Message.builder()
                .role(targetRole)
                .content(cm.getContent())
                .build();
    }

    private String extractContent(GenerationResult result) {
        if (result == null) {
            return "";
        }
        GenerationOutput output = result.getOutput();
        if (output == null || output.getChoices() == null || output.getChoices().isEmpty()) {
            return "";
        }
        Message msg = output.getChoices().get(0).getMessage();
        return msg == null ? "" : msg.getContent();
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
            throw new RuntimeException("千问调用重试等待被中断", e);
        }
    }
}
