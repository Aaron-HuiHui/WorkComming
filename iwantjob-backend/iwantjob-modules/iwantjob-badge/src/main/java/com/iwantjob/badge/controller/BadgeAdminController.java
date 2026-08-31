package com.iwantjob.badge.controller;

import com.iwantjob.badge.dto.BadgeTemplateCreateDTO;
import com.iwantjob.badge.dto.BadgeTemplateVO;
import com.iwantjob.badge.service.BadgeTemplateService;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 徽章管理后台控制器
 * <p>
 * - GET  /admin/badges/templates 徽章模板管理列表
 * - POST /admin/badges/templates 新建徽章模板（@PreAuthorize ADMIN）
 * <p>
 * 角色映射见 {@link com.iwantjob.common.enums.UserRoleEnum#ADMIN}，code=9，
 * JwtAuthFilter 将角色映射为 Spring Security authority "ROLE_9"，
 * 故使用 hasRole('9') 进行 ADMIN 鉴权。
 */
@Slf4j
@RestController
@RequestMapping("/admin/badges")
@RequiredArgsConstructor
@Tag(name = "成就徽章", description = "徽章模板管理（管理员）")
public class BadgeAdminController {

    private final BadgeTemplateService badgeTemplateService;

    @GetMapping("/templates")
    @Operation(summary = "徽章模板管理列表（管理员）")
    @PreAuthorize("hasRole('9')")
    public Result<List<BadgeTemplateVO>> list() {
        return Result.success(badgeTemplateService.listTemplates());
    }

    @PostMapping("/templates")
    @Operation(summary = "新建徽章模板（管理员）")
    @PreAuthorize("hasRole('9')")
    @Idempotent(prefix = "admin:badge:create", expireSeconds = 600)
    public Result<Long> create(@Valid @RequestBody BadgeTemplateCreateDTO dto) {
        Long id = badgeTemplateService.createTemplate(dto);
        return Result.success(id);
    }
}
