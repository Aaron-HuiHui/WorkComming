package com.iwantjob.badge.controller;

import com.iwantjob.badge.dto.UserBadgeVO;
import com.iwantjob.badge.service.BadgeService;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户徽章控制器
 * <p>
 * - GET /user/badges 当前用户徽章（@PreAuthorize 已登录）
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "成就徽章", description = "当前用户徽章")
public class UserBadgeController {

    private final BadgeService badgeService;

    @GetMapping("/badges")
    @Operation(summary = "当前用户徽章列表（已登录）")
    @PreAuthorize("isAuthenticated()")
    public Result<List<UserBadgeVO>> myBadges() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(badgeService.listMyBadges(userId));
    }
}
