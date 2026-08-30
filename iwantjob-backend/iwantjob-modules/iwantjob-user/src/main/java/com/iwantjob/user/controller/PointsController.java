package com.iwantjob.user.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.user.dto.PointsVO;
import com.iwantjob.user.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分控制器
 */
@Slf4j
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "积分接口", description = "积分余额查询")
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/me")
    @Operation(summary = "我的积分余额")
    public Result<PointsVO> myPoints() {
        Long userId = SecurityUtils.requireCurrentUserId();
        PointsVO vo = pointsService.getMyPoints(userId);
        return Result.success(vo);
    }
}
