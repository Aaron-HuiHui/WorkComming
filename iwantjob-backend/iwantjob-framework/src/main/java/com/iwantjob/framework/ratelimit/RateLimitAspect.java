package com.iwantjob.framework.ratelimit;

import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 限流切面：Redis + Lua 令牌桶
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
            "local key = KEYS[1] " +
            "local rate = tonumber(ARGV[1]) " +
            "local capacity = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "local exist = redis.call('EXISTS', key) " +
            "if exist == 1 then " +
            "  redis.call('HSET', key, 'last_time', now) " +
            "else " +
            "  redis.call('HMSET', key, 'tokens', capacity, 'last_time', now) " +
            "  redis.call('EXPIRE', key, 60) " +
            "end " +
            "local tokens = tonumber(redis.call('HGET', key, 'tokens')) " +
            "local last_time = tonumber(redis.call('HGET', key, 'last_time')) " +
            "local delta = math.max(0, now - last_time) * rate " +
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

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        // 登录/注册等匿名接口没有 X-User-Id，若统一退化为 "anonymous" 会让所有未登录用户共享一个桶，
        // 高峰期互相挤兑误伤；匿名场景改按客户端 IP 隔离
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            } else {
                ip = ip.split(",")[0].trim();
            }
            userId = "ip:" + ip;
        }
        String key = "rate_limit:" + request.getRequestURI() + ":" + userId;

        Long allowed = redisTemplate.execute(RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(rateLimit.rate()),
                String.valueOf(rateLimit.capacity()),
                String.valueOf(System.currentTimeMillis()));

        if (allowed == null || allowed == 0) {
            log.warn("限流触发: key={}", key);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
        return pjp.proceed();
    }
}
