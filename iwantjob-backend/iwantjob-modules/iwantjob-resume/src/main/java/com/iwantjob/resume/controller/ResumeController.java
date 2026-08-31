package com.iwantjob.resume.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.ratelimit.RateLimit;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.resume.dto.ResumeCreateDTO;
import com.iwantjob.resume.dto.ResumeMatchVO;
import com.iwantjob.resume.dto.ResumeOptimizeDTO;
import com.iwantjob.resume.dto.ResumeOptimizeVO;
import com.iwantjob.resume.dto.ResumeScoreVO;
import com.iwantjob.resume.dto.ResumeUpdateDTO;
import com.iwantjob.resume.dto.ResumeVO;
import com.iwantjob.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历控制器
 * 权限标记：[S]学生 [A]校友 [H]HR [Admin]管理员
 * 简历属个人资产，CRUD 与 AI 操作限 [S][A]，校验归属在 service 层完成
 */
@Slf4j
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@Tag(name = "简历服务", description = "简历 CRUD、AI 优化/评分、简历-职位匹配度")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @Operation(summary = "创建简历")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "resume:create", expireSeconds = 600)
    public Result<Long> create(@Valid @RequestBody ResumeCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Long id = resumeService.createResume(userId, dto);
        return Result.success(id);
    }

    @GetMapping("/me")
    @Operation(summary = "我的简历列表")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<List<ResumeVO>> myList() {
        Long userId = SecurityUtils.requireCurrentUserId();
        List<ResumeVO> list = resumeService.listMyResumes(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "简历详情")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<ResumeVO> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ResumeVO vo = resumeService.getResumeDetail(userId, id);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新简历")
    @PreAuthorize("hasAnyRole('0','1')")
    @Idempotent(prefix = "resume:update", expireSeconds = 600)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ResumeUpdateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        resumeService.updateResume(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除简历（软删除）")
    @PreAuthorize("hasAnyRole('0','1')")
    @Idempotent(prefix = "resume:delete", expireSeconds = 60)
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        resumeService.deleteResume(userId, id);
        return Result.success();
    }

    @PostMapping("/optimize")
    @Operation(summary = "AI 简历优化（润色/翻译/强化）")
    @PreAuthorize("hasAnyRole('0','1')")
    @RateLimit(rate = 2, capacity = 4)           // AI 保护：2 QPS，桶容量 4
    @Idempotent(prefix = "resume:optimize", expireSeconds = 30)
    public Result<ResumeOptimizeVO> optimize(@Valid @RequestBody ResumeOptimizeDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ResumeOptimizeVO vo = resumeService.optimizeResume(userId, dto);
        return Result.success(vo);
    }

    @PostMapping("/score")
    @Operation(summary = "AI 简历评分（0-100）")
    @PreAuthorize("hasAnyRole('0','1')")
    @RateLimit(rate = 2, capacity = 4)
    @Idempotent(prefix = "resume:score", expireSeconds = 30)
    public Result<ResumeScoreVO> score(@RequestParam Long resumeId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ResumeScoreVO vo = resumeService.scoreResume(userId, resumeId);
        return Result.success(vo);
    }

    @GetMapping("/match")
    @Operation(summary = "简历-职位匹配度（关键词重叠率）")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<ResumeMatchVO> match(@RequestParam Long resumeId, @RequestParam Long jobId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        ResumeMatchVO vo = resumeService.matchJob(userId, resumeId, jobId);
        return Result.success(vo);
    }
}
