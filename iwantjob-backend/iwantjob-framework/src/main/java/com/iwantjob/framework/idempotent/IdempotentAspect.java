package com.iwantjob.framework.idempotent;

import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 幂等切面：基于 X-Idempotency-Key + Redis SETNX
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String key = request.getHeader("X-Idempotency-Key");
        if (key == null || key.isBlank()) {
            return pjp.proceed();
        }
        String redisKey = "idempotent:" + (idempotent.prefix().isEmpty() ? request.getRequestURI() : idempotent.prefix()) + ":" + key;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.expireSeconds()));
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("幂等拦截: key={}", redisKey);
            throw new BusinessException(ErrorCode.IDEMPOTENT_REPEAT);
        }
        return pjp.proceed();
    }
}
