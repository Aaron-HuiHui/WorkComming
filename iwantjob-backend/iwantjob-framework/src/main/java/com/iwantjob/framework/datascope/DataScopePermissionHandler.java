package com.iwantjob.framework.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.iwantjob.common.enums.UserRoleEnum;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限处理器（R2）：配合 MyBatis-Plus DataPermissionInterceptor 使用。
 * <p>
 * 解析 mappedStatementId 对应 Mapper 方法上的 {@link DataScope} 注解，
 * 将 {@code column = 当前用户ID} 拼入 where 条件。
 * <ul>
 *   <li>方法无注解：不干预，SQL 原样执行</li>
 *   <li>未登录（定时任务/内部调用）：不干预，由调用方自行控制</li>
 *   <li>管理员（role=9）：放行</li>
 *   <li>其余：拼条件（SELF 用注解 column；HR_COMPANY 用 poster_id，列名可由注解覆盖）</li>
 * </ul>
 * 注解解析结果按 statementId 缓存，避免每次查询反射。
 */
@Slf4j
public class DataScopePermissionHandler implements DataPermissionHandler {

    /** statementId -> 注解（缺失缓存 empty，避免反复 Class.forName） */
    private final Map<String, Optional<DataScope>> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        Optional<DataScope> annoOpt = annotationCache.computeIfAbsent(
                mappedStatementId, this::resolveAnnotation);
        if (annoOpt.isEmpty()) {
            return where;
        }
        DataScope scope = annoOpt.get();

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            // 未登录（内部调用/定时任务）：不干预，公开性由调用方与 Security 路径控制
            return where;
        }
        Integer role = SecurityUtils.getCurrentRole();
        if (role != null && role == UserRoleEnum.ADMIN.getCode()) {
            // 管理员全量放行
            return where;
        }

        String column = scope.value() == ScopeType.HR_COMPANY
                ? scope.column().equals("user_id") ? "poster_id" : scope.column()
                : scope.column();
        EqualsTo cond = new EqualsTo(new Column(column), new LongValue(userId));
        log.debug("[DataScope] {} 追加条件: {} = {}", mappedStatementId, column, userId);
        return where == null ? cond : new AndExpression(where, cond);
    }

    /**
     * 从 mappedStatementId 解析 Mapper 方法上的 @DataScope 注解
     */
    private Optional<DataScope> resolveAnnotation(String mappedStatementId) {
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot <= 0) {
            return Optional.empty();
        }
        String className = mappedStatementId.substring(0, lastDot);
        String methodName = mappedStatementId.substring(lastDot + 1);
        try {
            Class<?> mapperInterface = Class.forName(className);
            for (Method method : mapperInterface.getMethods()) {
                if (method.getName().equals(methodName)
                        && method.isAnnotationPresent(DataScope.class)) {
                    return Optional.of(method.getAnnotation(DataScope.class));
                }
            }
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            log.warn("[DataScope] Mapper 类不存在，跳过数据权限: {}", className);
            return Optional.empty();
        }
    }
}
