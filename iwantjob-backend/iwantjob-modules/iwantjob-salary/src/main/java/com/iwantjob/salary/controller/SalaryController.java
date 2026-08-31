package com.iwantjob.salary.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.ratelimit.RateLimit;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.salary.dto.SalaryContributeDTO;
import com.iwantjob.salary.dto.SalaryContributionVO;
import com.iwantjob.salary.dto.WhitepaperVO;
import com.iwantjob.salary.service.SalaryContributeService;
import com.iwantjob.salary.service.SalaryWhitepaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 薪资白皮书控制器（用户端）
 * 权限标记：[S]学生 [A]校友 [已登录]
 */
@Slf4j
@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
@Tag(name = "薪资白皮书", description = "脱敏薪资贡献与白皮书查询")
public class SalaryController {

    private final SalaryContributeService salaryContributeService;
    private final SalaryWhitepaperService salaryWhitepaperService;

    @PostMapping("/contribute")
    @Operation(summary = "提交脱敏薪资数据（幂等+Redis去重）")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "salary:contribute", expireSeconds = 600)
    @RateLimit(rate = 5, capacity = 10)
    public Result<Long> contribute(@Valid @RequestBody SalaryContributeDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Long id = salaryContributeService.contribute(userId, dto);
        return Result.success(id);
    }

    @GetMapping("/contributions/me")
    @Operation(summary = "我的贡献列表")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    public Result<PageResult<SalaryContributionVO>> myContributions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<SalaryContributionVO> result = salaryContributeService.getMyContributions(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/whitepaper/latest")
    @Operation(summary = "最新白皮书（简版公开，高级章节需贡献记录）")
    public Result<WhitepaperVO> latestWhitepaper() {
        Long userId = SecurityUtils.getCurrentUserId();
        WhitepaperVO vo = salaryWhitepaperService.getLatest(userId);
        return Result.success(vo);
    }

    @GetMapping("/whitepaper/{id}")
    @Operation(summary = "指定版本白皮书")
    public Result<WhitepaperVO> whitepaperById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        WhitepaperVO vo = salaryWhitepaperService.getById(userId, id);
        return Result.success(vo);
    }
}
