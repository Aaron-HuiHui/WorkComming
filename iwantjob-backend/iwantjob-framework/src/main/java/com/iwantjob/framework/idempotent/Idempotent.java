package com.iwantjob.framework.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等注解，配合 X-Idempotency-Key 头使用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等key前缀，默认用方法路径
     */
    String prefix() default "";

    /**
     * 过期秒数，默认600秒
     */
    int expireSeconds() default 600;
}
