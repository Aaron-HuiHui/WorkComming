package com.iwantjob.simulator.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.ratelimit.RateLimit;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.simulator.dto.ChooseDTO;
import com.iwantjob.simulator.dto.ChooseVO;
import com.iwantjob.simulator.dto.ScenarioDetailVO;
import com.iwantjob.simulator.dto.ScenarioVO;
import com.iwantjob.simulator.dto.SessionHistoryVO;
import com.iwantjob.simulator.dto.SessionReportVO;
import com.iwantjob.simulator.dto.SessionStartVO;
import com.iwantjob.simulator.service.SimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 职业模拟舱控制器
 * <p>权限标记：[S]学生 [A]校友 [T]导师 [已登录]（参考角色权限矩阵）</p>
 * <p>所有会话操作仅限本人；写接口加 @Idempotent；AI 类接口加 @RateLimit 保护千问配额</p>
 */
@Slf4j
@RestController
@RequestMapping("/simulator")
@RequiredArgsConstructor
@Tag(name = "职业模拟舱", description = "AI职业模拟舱：场景列表、开始会话、提交选择、报告、历史")
public class SimulatorController {

    private final SimulatorService simulatorService;

    @GetMapping("/scenarios")
    @Operation(summary = "可用场景列表（is_active=1）")
    @PreAuthorize("hasAnyRole('0','1','3','9')")  // [S]学生 [A]校友 [T]导师 [Admin]
    public Result<List<ScenarioVO>> listScenarios() {
        List<ScenarioVO> list = simulatorService.listActiveScenarios();
        return Result.success(list);
    }

    @GetMapping("/scenarios/{id}")
    @Operation(summary = "场景详情（含起始节点）")
    @PreAuthorize("hasAnyRole('0','1','3','9')")  // [S]学生 [A]校友 [T]导师 [Admin]
    public Result<ScenarioDetailVO> getScenario(@PathVariable("id") Long id) {
        ScenarioDetailVO vo = simulatorService.getScenarioDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/start")
    @Operation(summary = "开始模拟会话，返回起始节点描述+选项")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "simulator:start", expireSeconds = 600)
    @RateLimit(rate = 2, capacity = 5)  // AI 接口限流：保护配额
    public Result<SessionStartVO> start(@RequestParam Long scenarioId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        SessionStartVO vo = simulatorService.start(userId, scenarioId);
        return Result.success(vo);
    }

    @PostMapping("/choose")
    @Operation(summary = "提交选择：写 choice、AI 反馈、推进节点，到达终点则完成+评分+触发事件")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @RateLimit(rate = 4, capacity = 10)  // AI 接口限流
    public Result<ChooseVO> choose(@Valid @RequestBody ChooseDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ChooseVO vo = simulatorService.choose(userId, dto);
        return Result.success(vo);
    }

    @GetMapping("/session/{id}/report")
    @Operation(summary = "会话报告（含所有 choice + 评分，仅会话所有者）")
    @PreAuthorize("hasAnyRole('0','1','3','9')")  // [S]学生 [A]校友 [T]导师 [Admin]
    public Result<SessionReportVO> getReport(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        SessionReportVO vo = simulatorService.getReport(userId, id);
        return Result.success(vo);
    }

    @GetMapping("/sessions/me")
    @Operation(summary = "我的模拟历史（分页）")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    public Result<PageResult<SessionHistoryVO>> mySessions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<SessionHistoryVO> result = simulatorService.mySessions(userId, page, size);
        return Result.success(result);
    }
}
