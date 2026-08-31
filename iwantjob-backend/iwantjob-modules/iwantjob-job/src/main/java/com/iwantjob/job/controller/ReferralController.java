package com.iwantjob.job.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.job.dto.ReferralCreateDTO;
import com.iwantjob.job.dto.ReferralVO;
import com.iwantjob.job.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内推控制器
 */
@Slf4j
@RestController
@RequestMapping("/referrals")
@RequiredArgsConstructor
@Tag(name = "职位服务", description = "内推码管理")
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    @Operation(summary = "创建内推码")
    @PreAuthorize("hasRole('1')")  // [A]校友
    @Idempotent(prefix = "referral:create", expireSeconds = 600)
    public Result<ReferralVO> create(@Valid @RequestBody ReferralCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ReferralVO vo = referralService.createReferral(userId, dto);
        return Result.success(vo);
    }
}
