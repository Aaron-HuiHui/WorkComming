package com.iwantjob.framework.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解，标记需要记录审计日志的操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作类型，如 SALARY_REVIEW / BADGE_LOCK / POINT_ADJUST / USER_BAN
     */
    String action();

    /**
     * 操作目标类型
     */
    String targetType() default "";
}
