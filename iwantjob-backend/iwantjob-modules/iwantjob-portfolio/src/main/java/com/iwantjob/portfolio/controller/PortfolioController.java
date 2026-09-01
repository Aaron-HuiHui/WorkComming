package com.iwantjob.portfolio.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.portfolio.dto.PortfolioSaveDTO;
import com.iwantjob.portfolio.dto.PortfolioVO;
import com.iwantjob.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 作品集控制器
 * 权限标记：[S]学生 [A]校友 [已登录]
 */
@Slf4j
@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "作品集", description = "学生作品展览分享：广场浏览、发布管理、点赞")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "作品广场（分页，可按技术标签过滤）")
    public Result<PageResult<PortfolioVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String tag) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<PortfolioVO> result = portfolioService.pagePortfolios(userId, page, size, tag);
        return Result.success(result);
    }

    @GetMapping("/me")
    @Operation(summary = "我的作品列表")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    public Result<PageResult<PortfolioVO>> mine(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<PortfolioVO> result = portfolioService.pageMyPortfolios(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "作品详情（浏览量+1）")
    public Result<PortfolioVO> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PortfolioVO vo = portfolioService.getPortfolioDetail(userId, id);
        return Result.success(vo);
    }

    @PostMapping
    @Operation(summary = "发布作品")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "portfolio:create", expireSeconds = 60)
    public Result<Long> create(@Valid @RequestBody PortfolioSaveDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Long id = portfolioService.createPortfolio(userId, dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新作品（仅作者本人）")
    @PreAuthorize("hasAnyRole('0','1')")
    @Idempotent(prefix = "portfolio:update", expireSeconds = 30)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PortfolioSaveDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        portfolioService.updatePortfolio(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除作品（软删除，仅作者本人）")
    @PreAuthorize("hasAnyRole('0','1')")
    @Idempotent(prefix = "portfolio:delete", expireSeconds = 30)
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        portfolioService.deletePortfolio(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞切换")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Object[] r = portfolioService.toggleLike(userId, id);
        return Result.success(Map.of("liked", r[0], "likeCount", r[1]));
    }
}