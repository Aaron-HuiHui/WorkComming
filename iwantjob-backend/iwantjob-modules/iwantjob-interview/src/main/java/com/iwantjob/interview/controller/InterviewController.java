package com.iwantjob.interview.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.ratelimit.RateLimit;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.interview.dto.InterviewAnswerDTO;
import com.iwantjob.interview.dto.InterviewAnswerVO;
import com.iwantjob.interview.dto.InterviewDetailVO;
import com.iwantjob.interview.dto.InterviewEndVO;
import com.iwantjob.interview.dto.InterviewHistoryVO;
import com.iwantjob.interview.dto.InterviewStartDTO;
import com.iwantjob.interview.dto.InterviewStartVO;
import com.iwantjob.interview.service.InterviewService;
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

/**
 * 模拟面试控制器
 * <p>权限标记：[S]学生 [A]校友 [已登录]</p>
 */
@Slf4j
@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
@Tag(name = "面试服务", description = "模拟面试：创建会话、提交回答、历史、详情、结束评分")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    @Operation(summary = "创建面试会话，从题库随机抽5题，返回第一题")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "interview:start", expireSeconds = 600)
    @RateLimit(rate = 2, capacity = 5)  // AI 接口限流：保护配额
    public Result<InterviewStartVO> start(@Valid @RequestBody InterviewStartDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        InterviewStartVO vo = interviewService.start(userId, dto);
        return Result.success(vo);
    }

    @PostMapping("/answer")
    @Operation(summary = "提交回答，AI反馈并返回下一题")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @RateLimit(rate = 4, capacity = 10)  // AI 接口限流
    public Result<InterviewAnswerVO> answer(@Valid @RequestBody InterviewAnswerDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        InterviewAnswerVO vo = interviewService.answer(userId, dto);
        return Result.success(vo);
    }

    @GetMapping("/history")
    @Operation(summary = "面试历史分页（仅本人）")
    public Result<PageResult<InterviewHistoryVO>> history(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<InterviewHistoryVO> result = interviewService.history(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/{mockId}")
    @Operation(summary = "面试详情（含全部题目）")
    public Result<InterviewDetailVO> detail(@PathVariable Long mockId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        InterviewDetailVO vo = interviewService.detail(userId, mockId);
        return Result.success(vo);
    }

    @PostMapping("/{mockId}/end")
    @Operation(summary = "结束面试，生成评分汇总")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "interview:end", expireSeconds = 600)
    @RateLimit(rate = 2, capacity = 5)  // AI 接口限流
    public Result<InterviewEndVO> end(@PathVariable Long mockId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        InterviewEndVO vo = interviewService.end(userId, mockId);
        return Result.success(vo);
    }

    @GetMapping("/questions")
    @Operation(summary = "题库分页浏览（学生学习中心，可按分类过滤）")
    public Result<PageResult<com.iwantjob.interview.dto.QuestionBankVO>> questions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String subCategory) {
        SecurityUtils.requireCurrentUserId();
        PageResult<com.iwantjob.interview.dto.QuestionBankVO> result = interviewService.listQuestions(page, size, category, subCategory);
        return Result.success(result);
    }

    @GetMapping("/questions/{id}")
    @Operation(summary = "题目详情（含考点关键词）")
    public Result<com.iwantjob.interview.dto.QuestionBankVO> questionDetail(@PathVariable Long id) {
        SecurityUtils.requireCurrentUserId();
        com.iwantjob.interview.dto.QuestionBankVO vo = interviewService.questionDetail(id);
        return Result.success(vo);
    }}
