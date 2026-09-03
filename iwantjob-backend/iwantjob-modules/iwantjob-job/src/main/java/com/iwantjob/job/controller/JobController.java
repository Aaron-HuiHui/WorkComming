package com.iwantjob.job.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.idempotent.Idempotent;
import com.iwantjob.framework.security.SecurityUtils;
import com.iwantjob.job.dto.ApplicationStatusDTO;
import com.iwantjob.job.dto.CandidateDetailVO;
import com.iwantjob.job.dto.CandidateVO;
import com.iwantjob.job.dto.HrJobVO;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.dto.JobApplyDTO;
import com.iwantjob.job.dto.JobCreateDTO;
import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.dto.JobStatsVO;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.service.JobApplicationService;
import com.iwantjob.job.service.JobService;
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
 * 职位控制器
 * 权限标记：[S]学生 [A]校友 [H]HR [Admin]管理员
 */
@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "职位服务", description = "职位搜索、详情、发布、投递、HR候选人管理、岗位统计")
public class JobController {

    private final JobService jobService;
    private final JobApplicationService jobApplicationService;

    @GetMapping
    @Operation(summary = "职位搜索（MySQL FULLTEXT 兜底）")
    public Result<PageResult<JobVO>> search(@Valid JobSearchDTO param) {
        SecurityUtils.requireCurrentUserId();
        PageResult<JobVO> page = jobService.searchJobs(param);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "职位详情（view_count + 1）")
    public Result<JobVO> detail(@PathVariable Long id) {
        SecurityUtils.requireCurrentUserId();
        JobVO vo = jobService.getJobDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    @Operation(summary = "发布职位")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    @Idempotent(prefix = "job:publish", expireSeconds = 600)
    public Result<Long> publish(@Valid @RequestBody JobCreateDTO dto) {
        Long posterId = SecurityUtils.requireCurrentUserId();
        Long id = jobService.publishJob(posterId, dto);
        return Result.success(id);
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "投递职位（校验重复投递）")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    @Idempotent(prefix = "job:apply", expireSeconds = 600)
    public Result<Long> apply(@PathVariable Long id, @Valid @RequestBody JobApplyDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Long appid = jobApplicationService.applyJob(userId, id, dto);
        return Result.success(appid);
    }

    @GetMapping("/me/applied")
    @Operation(summary = "我投递的职位列表")
    @PreAuthorize("hasAnyRole('0','1')")  // [S]学生 [A]校友
    public Result<PageResult<JobApplicationVO>> myApplied(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        PageResult<JobApplicationVO> result = jobApplicationService.getMyApplied(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/me/applied-ids")
    @Operation(summary = "我投递过的职位ID集合（前端回显已投递态）")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result<List<Long>> myAppliedIds() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(jobApplicationService.getMyAppliedJobIds(userId));
    }

    // ==================== 岗位市场统计（学生可视化） ====================

    @GetMapping("/stats/overview")
    @Operation(summary = "岗位市场统计总览（城市/类型/薪资分布 + 热门职位）")
    public Result<JobStatsVO> statsOverview() {
        SecurityUtils.requireCurrentUserId();
        JobStatsVO vo = jobService.getStatsOverview();
        return Result.success(vo);
    }

    // ==================== HR 候选人管理 ====================

    @GetMapping("/me/published")
    @Operation(summary = "我发布的职位列表（含投递数统计，HR 工作台）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    public Result<PageResult<HrJobVO>> myPublished(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long posterId = SecurityUtils.requireCurrentUserId();
        PageResult<HrJobVO> result = jobService.getMyPublishedJobs(posterId, page, size);
        return Result.success(result);
    }

    @GetMapping("/{jobId}/applications")
    @Operation(summary = "某职位的投递者列表（HR 视角，校验职位归属）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    public Result<PageResult<CandidateVO>> jobApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long hrUserId = SecurityUtils.requireCurrentUserId();
        PageResult<CandidateVO> result = jobApplicationService.getJobCandidates(hrUserId, jobId, page, size);
        return Result.success(result);
    }

    @GetMapping("/applications/{appId}/candidate")
    @Operation(summary = "候选人详情（基本资料+徽章+简历，校验职位归属）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    public Result<CandidateDetailVO> candidateDetail(@PathVariable Long appId) {
        Long hrUserId = SecurityUtils.requireCurrentUserId();
        CandidateDetailVO vo = jobApplicationService.getCandidateDetail(hrUserId, appId);
        return Result.success(vo);
    }

    @PutMapping("/applications/{appId}/status")
    @Operation(summary = "更新投递状态（初筛/面试/录用/拒绝 + HR备注）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    @Idempotent(prefix = "job:appstatus", expireSeconds = 5)
    public Result<Void> updateApplicationStatus(
            @PathVariable Long appId,
            @Valid @RequestBody ApplicationStatusDTO dto) {
        Long hrUserId = SecurityUtils.requireCurrentUserId();
        jobApplicationService.updateApplicationStatus(hrUserId, appId, dto);
        return Result.success();
    }

    // ==================== 职位下架/删除（同步删 ES 索引） ====================

    @PutMapping("/{id}/offline")
    @Operation(summary = "下架职位（status→0，同步删除ES索引，仅发布者/管理员）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    @Idempotent(prefix = "job:offline", expireSeconds = 5)
    public Result<Void> offline(@PathVariable Long id) {
        Long operatorId = SecurityUtils.requireCurrentUserId();
        jobService.offlineJob(operatorId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位（软删除，同步删除ES索引，仅发布者/管理员）")
    @PreAuthorize("hasAnyRole('1','2','9')")  // [A]校友 [H]HR [Admin]管理员
    @Idempotent(prefix = "job:delete", expireSeconds = 5)
    public Result<Void> delete(@PathVariable Long id) {
        Long operatorId = SecurityUtils.requireCurrentUserId();
        jobService.deleteJob(operatorId, id);
        return Result.success();
    }
}