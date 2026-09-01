package com.iwantjob.framework.datascope;

import com.iwantjob.framework.security.LoginUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据权限处理器单元测试（R2）：
 * 注解解析、未登录放行、管理员放行、SELF/HR_COMPANY 条件拼接、已有 where 组合
 */
class DataScopePermissionHandlerTest {

    /** 测试用 Mapper：三类方法（SELF / HR_COMPANY / 无注解） */
    interface ScopedMapper {
        @DataScope(value = ScopeType.SELF, column = "a.user_id")
        List<Object> myData();

        @DataScope(ScopeType.HR_COMPANY)
        List<Object> hrJobs();

        List<Object> noScope();
    }

    private static final String MSID_SELF =
            "com.iwantjob.framework.datascope.DataScopePermissionHandlerTest$ScopedMapper.myData";
    private static final String MSID_HR =
            "com.iwantjob.framework.datascope.DataScopePermissionHandlerTest$ScopedMapper.hrJobs";
    private static final String MSID_NONE =
            "com.iwantjob.framework.datascope.DataScopePermissionHandlerTest$ScopedMapper.noScope";

    private final DataScopePermissionHandler handler = new DataScopePermissionHandler();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long userId, Integer role) {
        LoginUser principal = new LoginUser(userId, "user" + userId, role);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void methodWithoutAnnotationShouldReturnOriginalWhere() {
        Expression original = new EqualsTo();
        Expression result = handler.getSqlSegment(original, MSID_NONE);
        assertSame(original, result);
    }

    @Test
    void anonymousCallShouldReturnOriginalWhere() {
        // 未登录：不干预
        Expression result = handler.getSqlSegment(null, MSID_SELF);
        assertNull(result);
    }

    @Test
    void adminShouldBypassDataScope() {
        loginAs(1L, 9);
        Expression original = new EqualsTo();
        Expression result = handler.getSqlSegment(original, MSID_SELF);
        assertSame(original, result);
    }

    @Test
    void studentSelfScopeShouldAppendUserIdCondition() {
        loginAs(42L, 0);
        Expression result = handler.getSqlSegment(null, MSID_SELF);
        assertEquals("a.user_id = 42", result.toString());
    }

    @Test
    void selfScopeShouldCombineWithExistingWhere() {
        loginAs(42L, 0);
        EqualsTo existing = new EqualsTo(
                new net.sf.jsqlparser.schema.Column("is_deleted"), new LongValue(0));
        Expression result = handler.getSqlSegment(existing, MSID_SELF);
        // 组合后包含两个条件
        String sql = result.toString();
        assertTrue(sql.contains("is_deleted = 0") && sql.contains("a.user_id = 42"),
                "组合条件应同时包含原 where 与数据权限条件，实际: " + sql);
    }

    @Test
    void hrCompanyScopeShouldUsePosterIdColumn() {
        loginAs(7L, 2);
        Expression result = handler.getSqlSegment(null, MSID_HR);
        assertEquals("poster_id = 7", result.toString());
    }

    @Test
    void hrCompanyScopeShouldAlsoApplyToNonHrRoleAsFallback() {
        // 非 HR 用户调用 HR 方法：同样拼条件（兜底防越权，结果为空集）
        loginAs(8L, 0);
        Expression result = handler.getSqlSegment(null, MSID_HR);
        assertEquals("poster_id = 8", result.toString());
    }

    @Test
    void annotationCacheShouldResolveAcrossCalls() {
        loginAs(1L, 0);
        // 第一次调用触发反射解析并缓存
        assertEquals("a.user_id = 1", handler.getSqlSegment(null, MSID_SELF).toString());
        // 第二次调用走缓存，结果一致
        assertEquals("a.user_id = 1", handler.getSqlSegment(null, MSID_SELF).toString());
    }

    @Test
    void unknownStatementShouldReturnOriginalWhere() {
        loginAs(1L, 0);
        Expression original = new EqualsTo();
        Expression result = handler.getSqlSegment(original, "com.example.not.exists.Mapper.query");
        assertSame(original, result);
    }
}
