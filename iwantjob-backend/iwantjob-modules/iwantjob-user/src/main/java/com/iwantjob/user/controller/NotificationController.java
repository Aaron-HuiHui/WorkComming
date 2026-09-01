package com.iwantjob.user.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.user.dto.NotificationVO;
import com.iwantjob.user.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知控制器（已登录即可）
 */
@Slf4j
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
@Tag(name = "站内通知", description = "通知列表/未读数/标记已读")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @Operation(summary = "我的通知分页")
    public Result<PageResult<NotificationVO>> me(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(notificationService.myNotifications(userId, page, size));
    }

    @GetMapping("/me/unread-count")
    @Operation(summary = "未读通知数量")
    public Result<Long> unreadCount() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(notificationService.unreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条已读")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        notificationService.markRead(userId, id);
        return Result.success();
    }

    @PutMapping("/me/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead() {
        Long userId = SecurityUtils.requireCurrentUserId();
        notificationService.markAllRead(userId);
        return Result.success();
    }
}