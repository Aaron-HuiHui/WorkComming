package com.iwantjob.framework.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解（R2 实现：MyBatis-Plus DataPermissionInterceptor 拼接 SQL 条件）
 * <p>
 * 标注在 Mapper 查询方法上，由
 * {@link com.iwantjob.framework.config.MyBatisPlusConfig} 注册的
 * {@link com.iwantjob.framework.datascope.DataScopePermissionHandler} 解析：
 * <ul>
 *   <li>方法无注解：不干预，SQL 原样执行</li>
 *   <li>当前未登录（定时任务/内部调用）：不干预，由调用方自行控制</li>
 *   <li>管理员（role=9）：放行，不拼条件</li>
 *   <li>其余情况：按 {@link #value()} 拼接 {@code column = 当前用户ID} 条件</li>
 * </ul>
 * <p>
 * 使用示例（联表查询可带表前缀）：
 * <pre>
 * &#64;DataScope(value = ScopeType.SELF, column = "a.user_id")
 * IPage&lt;JobApplicationVO&gt; selectMyApplied(IPage&lt;JobApplicationVO&gt; page, @Param("userId") Long userId);
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 数据范围类型，默认本人数据
     */
    ScopeType value() default ScopeType.SELF;

    /**
     * 用户列名，支持表前缀（如 "a.user_id"）；HR_COMPANY 默认 poster_id
     */
    String column() default "user_id";
}
