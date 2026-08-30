package com.iwantjob.framework.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解（令牌桶，按用户+接口维度）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 每秒令牌数
     */
    int rate() default 10;

    /**
     * 桶容量
     */
    int capacity() default 20;
}
