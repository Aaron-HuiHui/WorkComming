package com.iwantjob.community.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.community.dto.SkillExchangeCreateDTO;
import com.iwantjob.community.dto.SkillExchangeVO;
import com.iwantjob.community.service.SkillExchangeService;
import com.iwantjob.framework.idempotent.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技能交换接口
 */
@Tag(name = "社区服务", description = "帖子、回答、技能交换、众筹")
@RestController
@RequestMapping("/skill-exchanges")
@RequiredArgsConstructor
public class SkillExchangeController {

    private final SkillExchangeService skillExchangeService;

    @Operation(summary = "发起技能交换")
    @PostMapping
    @Idempotent
    public Result<SkillExchangeVO> createSkillExchange(@RequestBody @Valid SkillExchangeCreateDTO dto) {
        return Result.success(skillExchangeService.createSkillExchange(dto));
    }

    @Operation(summary = "我的技能交换")
    @GetMapping("/me")
    public Result<List<SkillExchangeVO>> mySkillExchanges() {
        return Result.success(skillExchangeService.mySkillExchanges());
    }
}
