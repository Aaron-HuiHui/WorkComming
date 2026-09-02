package com.iwantjob.user.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.ratelimit.RateLimit;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.user.dto.LoginDTO;
import com.iwantjob.user.dto.LoginVO;
import com.iwantjob.user.dto.RefreshDTO;
import com.iwantjob.user.dto.RegisterDTO;
import com.iwantjob.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "注册、登录、刷新、登出")
public class AuthController {

    private final AuthService authService;

    private static final String BEARER_PREFIX = "Bearer ";

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @Idempotent(prefix = "auth:register", expireSeconds = 600)
    @RateLimit(rate = 2, capacity = 5)
    public Result<Long> register(@Valid @RequestBody RegisterDTO dto) {
        Long userId = authService.register(dto);
        return Result.success(userId);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @RateLimit(rate = 5, capacity = 10)
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新token")
    @RateLimit(rate = 3, capacity = 10)
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshDTO dto) {
        LoginVO vo = authService.refresh(dto);
        return Result.success(vo);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出（当前token入黑名单）")
    public Result<Void> logout(HttpServletRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        // 解析当前accessToken
        String token = null;
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            token = authorization.substring(BEARER_PREFIX.length());
        }
        // 兜底：从自定义header取
        if (token == null) {
            String header = request.getHeader("X-Access-Token");
            if (header != null && !header.isBlank()) {
                token = header;
            }
        }
        // 调用方在 JwtAuthFilter 之后才到这里，能进入说明已通过认证；直接登出
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("用户登出请求: userId={}", userId);
        authService.logout(token);
        return Result.success();
    }
}
