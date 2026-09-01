package com.iwantjob.framework.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT 工具类单元测试：签发 / 解析 / 类型区分 / 篡改拒绝
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "unit-test-secret-key-must-be-at-least-32-chars!!");
        ReflectionTestUtils.setField(jwtUtils, "accessExpiration", 7200L);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpiration", 604800L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolderCleaner.clean();
    }

    @Test
    void accessTokenShouldCarryUserClaims() {
        String token = jwtUtils.generateAccessToken(1L, "alice", 0);
        assertNotNull(token);
        assertEquals(1L, jwtUtils.getUserId(token));
        assertEquals("alice", jwtUtils.getUsername(token));
        assertEquals(0, jwtUtils.getRole(token));
        assertEquals("ACCESS", jwtUtils.getTokenType(token));
    }

    @Test
    void refreshTokenShouldDistinguishType() {
        String token = jwtUtils.generateRefreshToken(2L, "bob", 9);
        assertEquals("REFRESH", jwtUtils.getTokenType(token));
        assertEquals(2L, jwtUtils.getUserId(token));
        assertEquals(9, jwtUtils.getRole(token));
    }

    @Test
    void validTokenShouldPassValidation() {
        String token = jwtUtils.generateAccessToken(1L, "alice", 0);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void tamperedTokenShouldFailValidation() {
        String token = jwtUtils.generateAccessToken(1L, "alice", 0);
        String tampered = token.substring(0, token.length() - 3) + "abc";
        assertFalse(jwtUtils.validateToken(tampered));
        assertThrows(Exception.class, () -> jwtUtils.parseToken(tampered));
    }

    @Test
    void garbageTokenShouldFailValidation() {
        assertFalse(jwtUtils.validateToken("not-a-jwt-token"));
    }

    @Test
    void expiredTokenShouldFailValidation() {
        JwtUtils shortLived = new JwtUtils();
        ReflectionTestUtils.setField(shortLived, "secret", "unit-test-secret-key-must-be-at-least-32-chars!!");
        ReflectionTestUtils.setField(shortLived, "accessExpiration", -10L);
        ReflectionTestUtils.setField(shortLived, "refreshExpiration", -10L);
        String expired = shortLived.generateAccessToken(1L, "alice", 0);
        assertFalse(jwtUtils.validateToken(expired));
    }

    @Test
    void parseValidTokenShouldReturnClaims() {
        String token = jwtUtils.generateAccessToken(3L, "carol", 1);
        Claims claims = jwtUtils.parseToken(token);
        assertEquals("carol", claims.getSubject());
        assertEquals(3L, claims.get("userId", Long.class));
    }

    @Test
    void expirationConfigShouldBeExposed() {
        assertEquals(7200L, jwtUtils.getAccessExpiration());
    }

    /** 辅助：清理 SecurityContext，避免测试间串扰 */
    private static final class SecurityContextHolderCleaner {
        static void clean() {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
