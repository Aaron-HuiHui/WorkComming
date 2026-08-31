package com.iwantjob.community.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.community.dto.CrowdfundingCreateDTO;
import com.iwantjob.community.dto.CrowdfundingSupportDTO;
import com.iwantjob.community.dto.CrowdfundingVO;
import com.iwantjob.community.service.CrowdfundingService;
import com.iwantjob.framework.idempotent.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 众筹接口
 */
@Tag(name = "社区服务", description = "帖子、回答、技能交换、众筹")
@RestController
@RequestMapping("/crowdfunding")
@RequiredArgsConstructor
public class CrowdfundingController {

    private final CrowdfundingService crowdfundingService;

    @Operation(summary = "发起众筹")
    @PostMapping
    @Idempotent
    public Result<CrowdfundingVO> createCrowdfunding(@RequestBody @Valid CrowdfundingCreateDTO dto) {
        return Result.success(crowdfundingService.createCrowdfunding(dto));
    }

    @Operation(summary = "支持众筹")
    @PostMapping("/{id}/support")
    @Idempotent
    public Result<CrowdfundingVO> supportCrowdfunding(@PathVariable Long id,
                                                      @RequestBody @Valid CrowdfundingSupportDTO dto) {
        return Result.success(crowdfundingService.supportCrowdfunding(id, dto));
    }
}
