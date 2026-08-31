package com.iwantjob.helpgroup.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.helpgroup.dto.HelpRequestCreateDTO;
import com.iwantjob.helpgroup.dto.HelpRequestListVO;
import com.iwantjob.helpgroup.dto.HelpRequestQueryDTO;
import com.iwantjob.helpgroup.dto.HelpRequestResolveDTO;
import com.iwantjob.helpgroup.dto.HelpRequestVO;
import com.iwantjob.helpgroup.service.HelpGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帮帮团接口
 */
@Tag(name = "帮帮团", description = "求职求助发起、支援者匹配与完成反馈")
@RestController
@RequestMapping("/help-group")
@RequiredArgsConstructor
public class HelpGroupController {

    private final HelpGroupService helpGroupService;

    @Operation(summary = "发起求助")
    @PostMapping("/request")
    @Idempotent
    public Result<HelpRequestVO> createRequest(@RequestBody @Valid HelpRequestCreateDTO dto) {
        return Result.success(helpGroupService.createRequest(dto));
    }

    @Operation(summary = "待匹配求助列表（分页，排除自己发起的）")
    @GetMapping("/requests")
    public Result<PageResult<HelpRequestListVO>> pagePendingRequests(@Valid HelpRequestQueryDTO query) {
        return Result.success(helpGroupService.pagePendingRequests(query));
    }

    @Operation(summary = "匹配支援者（当前用户成为 supporter）")
    @PostMapping("/{id}/match")
    @Idempotent
    public Result<HelpRequestVO> matchRequest(@PathVariable Long id) {
        return Result.success(helpGroupService.matchRequest(id));
    }

    @Operation(summary = "我的求助 / 我支援的（按当前用户角色查询）")
    @GetMapping("/me")
    public Result<PageResult<HelpRequestListVO>> pageMyRequests(@Valid HelpRequestQueryDTO query) {
        return Result.success(helpGroupService.pageMyRequests(query));
    }

    @Operation(summary = "完成支援（写 feedback，触发徽章与积分事件给 supporter）")
    @PutMapping("/{id}/resolve")
    @Idempotent
    public Result<HelpRequestVO> resolveRequest(@PathVariable Long id,
                                                 @RequestBody @Valid HelpRequestResolveDTO dto) {
        return Result.success(helpGroupService.resolveRequest(id, dto));
    }
}
