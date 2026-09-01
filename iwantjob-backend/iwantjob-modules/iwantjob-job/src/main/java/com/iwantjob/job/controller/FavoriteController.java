package com.iwantjob.job.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 职位收藏控制器
 * 权限标记：[S]学生 [A]校友
 */
@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "职位收藏", description = "职位收藏/取消/我的收藏列表")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏/取消收藏切换")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "job:fav", expireSeconds = 2)
    public Result<Map<String, Object>> toggle(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Boolean favored = favoriteService.toggleFavorite(userId, id);
        return Result.success(Map.of("favored", favored));
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "我的收藏职位列表")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<PageResult<JobVO>> myFavorites(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(favoriteService.getMyFavorites(userId, page, size));
    }

    @GetMapping("/me/favorite-ids")
    @Operation(summary = "我收藏的职位ID集合")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<List<Long>> favoriteIds() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(favoriteService.getFavoriteIds(userId));
    }
}