package com.iwantjob.salary.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.audit.AuditLog;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.salary.dto.PendingSalaryVO;
import com.iwantjob.salary.dto.SalaryReviewDTO;
import com.iwantjob.salary.dto.SalaryReviewLogVO;
import com.iwantjob.salary.service.SalaryReviewService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 薪资白皮书管理控制器（管理员端）
 * 权限标记：[Admin]管理员
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "薪资白皮书", description = "薪资数据审核与白皮书管理")
public class SalaryAdminController {

    private final SalaryReviewService salaryReviewService;
    private final SalaryWhitepaperService salaryWhitepaperService;

    @GetMapping("/salary/pending")
    @Operation(summary = "待审核薪资数据列表（含3σ异常标记）")
    @PreAuthorize("hasRole('9')")  // [Admin]管理员
    public Result<PageResult<PendingSalaryVO>> pendingList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        PageResult<PendingSalaryVO> result = salaryReviewService.getPendingList(page, size);
        return Result.success(result);
    }

    @PutMapping("/salary/{id}/review")
    @Operation(summary = "审核薪资数据（APPROVE奖励30分+解锁匹配；REJECT仅驳回）")
    @PreAuthorize("hasRole('9')")  // [Admin]管理员
    @AuditLog(action = "SALARY_REVIEW", targetType = "salary_report_data")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody SalaryReviewDTO dto) {
        Long reviewerId = SecurityUtils.requireCurrentUserId();
        salaryReviewService.review(reviewerId, id, dto);
        return Result.success();
    }

    @GetMapping("/salary/{id}/review-logs")
    @Operation(summary = "查看审核日志")
    @PreAuthorize("hasRole('9')")  // [Admin]管理员
    public Result<List<SalaryReviewLogVO>> reviewLogs(@PathVariable Long id) {
        List<SalaryReviewLogVO> logs = salaryReviewService.getReviewLogs(id);
        return Result.success(logs);
    }

    @PostMapping("/whitepaper/generate")
    @Operation(summary = "手动触发白皮书生成（聚合已审核数据计算分位值）")
    @PreAuthorize("hasRole('9')")  // [Admin]管理员
    @AuditLog(action = "WHITEPAPER_GENERATE", targetType = "salary_whitepaper")
    public Result<Long> generateWhitepaper() {
        Long id = salaryWhitepaperService.generateWhitepaper();
        return Result.success(id);
    }
}
