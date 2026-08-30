package com.iwantjob.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:iwantjob-default-secret-key-must-be-at-least-32-chars}")
    private String secret;

    @Value("${jwt.access-expiration:7200}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800}")
    private long refreshExpiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, Integer role) {
        return generateToken(userId, username, role, "ACCESS", accessExpiration);
    }

    public String generateRefreshToken(Long userId, String username, Integer role) {
        return generateToken(userId, username, role, "REFRESH", refreshExpiration);
    }

    private String generateToken(Long userId, String username, Integer role, String type, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", type);
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration * 1000))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("token校验失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Integer getRole(String token) {
        return parseToken(token).get("role", Integer.class);
    }

    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    public long getAccessExpiration() {
        return accessExpiration;
    }
}
