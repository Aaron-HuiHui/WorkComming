package com.iwantjob.framework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwantjob.framework.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志切面：记录关键操作到日志（后续可落 sys_audit_log 表）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        Long userId = SecurityUtils.getCurrentUserId();
        String ip = null;
        String ua = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            ip = req.getRemoteAddr();
            ua = req.getHeader("User-Agent");
        }
        Object result = pjp.proceed();
        try {
            log.info("AUDIT|userId={}|action={}|targetType={}|ip={}|ua={}|args={}",
                    userId, auditLog.action(), auditLog.targetType(), ip, ua,
                    objectMapper.writeValueAsString(pjp.getArgs()));
        } catch (Exception ignored) {
        }
        return result;
    }
}
