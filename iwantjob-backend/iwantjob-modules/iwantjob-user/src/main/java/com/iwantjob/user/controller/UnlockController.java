package com.iwantjob.user.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.user.dto.UnlockRecordVO;
import com.iwantjob.user.service.UnlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权益解锁控制器
 */
@Slf4j
@RestController
@RequestMapping("/unlock")
@RequiredArgsConstructor
@Tag(name = "权益解锁接口", description = "积分解锁就业权益")
public class UnlockController {

    private final UnlockService unlockService;

    @PostMapping("/mentor")
    @Operation(summary = "积分解锁导师咨询（扣减积分，乐观锁）")
    @Idempotent(prefix = "unlock:mentor", expireSeconds = 600)
    public Result<UnlockRecordVO> unlockMentor() {
        Long userId = SecurityUtils.requireCurrentUserId();
        UnlockRecordVO vo = unlockService.unlockMentor(userId);
        return Result.success(vo);
    }
}
