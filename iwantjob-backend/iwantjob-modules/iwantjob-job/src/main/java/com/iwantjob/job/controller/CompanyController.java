package com.iwantjob.job.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.job.dto.CompanyUpdateDTO;
import com.iwantjob.job.dto.CompanyVO;
import com.iwantjob.job.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企业信息控制器
 * 权限标记：[H]HR [Admin]管理员
 */
@Slf4j
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "企业信息", description = "知名企业主页：介绍/文化/福利，HR 认领编辑")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "企业列表（含在招职位数）")
    public Result<List<CompanyVO>> list(@RequestParam(required = false) String industry) {
        SecurityUtils.requireCurrentUserId();
        return Result.success(companyService.listCompanies(industry));
    }

    @GetMapping("/{id}")
    @Operation(summary = "企业详情")
    public Result<CompanyVO> detail(@PathVariable Long id) {
        SecurityUtils.requireCurrentUserId();
        return Result.success(companyService.getCompany(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "认领并编辑企业主页（须发布过该企业职位）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CompanyUpdateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Integer role = SecurityUtils.getCurrentRole();
        companyService.updateCompany(userId, role, id, dto);
        return Result.success();
    }
}