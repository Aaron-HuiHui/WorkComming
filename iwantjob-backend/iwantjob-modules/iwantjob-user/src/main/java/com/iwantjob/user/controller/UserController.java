package com.iwantjob.user.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.user.dto.ProfileUpdateDTO;
import com.iwantjob.user.dto.UserInfoVO;
import com.iwantjob.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "当前用户信息与个人资料")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "当前用户信息（含profile）")
    public Result<UserInfoVO> me() {
        Long userId = SecurityUtils.requireCurrentUserId();
        UserInfoVO vo = userService.getCurrentUserInfo(userId);
        return Result.success(vo);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        userService.updateProfile(userId, dto);
        return Result.success();
    }
}
