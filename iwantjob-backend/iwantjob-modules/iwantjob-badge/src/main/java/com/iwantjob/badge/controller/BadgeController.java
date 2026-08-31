package com.iwantjob.badge.controller;

import com.iwantjob.badge.dto.BadgeTemplateVO;
import com.iwantjob.badge.dto.BadgeVerifyVO;
import com.iwantjob.badge.dto.UserBadgeVO;
import com.iwantjob.badge.service.BadgeService;
import com.iwantjob.badge.service.BadgeTemplateService;
import com.iwantjob.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 徽章公开/已登录查询控制器
 * <p>
 * - GET /badges/templates     徽章模板列表（已登录）
 * - GET /badges/user/{userId} 公开主页徽章（含 lock_hash 前8位指纹）
 * - GET /badges/verify        企业查验（校验 lock_hash 匹配）
 */
@Slf4j
@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Tag(name = "成就徽章", description = "防篡改可信徽章：模板列表、公开主页徽章、企业查验")
public class BadgeController {

    private final BadgeTemplateService badgeTemplateService;
    private final BadgeService badgeService;

    @GetMapping("/templates")
    @Operation(summary = "徽章模板列表（已登录）")
    @PreAuthorize("isAuthenticated()")
    public Result<List<BadgeTemplateVO>> templates() {
        return Result.success(badgeTemplateService.listTemplates());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "公开主页徽章展示（含 lock_hash 前8位指纹）")
    @PreAuthorize("permitAll()")
    public Result<List<UserBadgeVO>> userBadges(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        return Result.success(badgeService.listUserBadges(userId));
    }

    @GetMapping("/verify")
    @Operation(summary = "企业查验徽章（校验 lock_hash 匹配）")
    @PreAuthorize("permitAll()")
    public Result<BadgeVerifyVO> verify(
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId,
            @Parameter(description = "徽章模板ID", required = true) @RequestParam("badgeId") Long badgeId,
            @Parameter(description = "lock_hash（完整64位）", required = true) @RequestParam("hash") String hash) {
        return Result.success(badgeService.verify(userId, badgeId, hash));
    }
}
