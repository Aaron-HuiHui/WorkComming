package com.iwantjob.framework.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                if (redisTemplate.hasKey(BLACKLIST_PREFIX + token)) {
                    log.debug("token在黑名单中");
                } else if (jwtUtils.validateToken(token)) {
                    Claims claims = jwtUtils.parseToken(token);
                    if ("ACCESS".equals(claims.get("type", String.class))) {
                        Long userId = claims.get("userId", Long.class);
                        String username = claims.getSubject();
                        Integer role = claims.get("role", Integer.class);
                        LoginUser loginUser = new LoginUser(userId, username, role);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getRole() == null ? java.util.List.of() : java.util.List.of(() -> "ROLE_" + loginUser.getRole()));
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                log.debug("token解析异常: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
