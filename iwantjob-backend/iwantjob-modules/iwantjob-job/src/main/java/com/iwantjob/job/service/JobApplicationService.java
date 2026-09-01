package com.iwantjob.job.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.ApplicationStatusDTO;
import com.iwantjob.job.dto.CandidateDetailVO;
import com.iwantjob.job.dto.CandidateVO;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.dto.JobApplyDTO;

/**
 * 职位投递服务接口
 */
public interface JobApplicationService {

    /**
     * 投递职位（校验重复投递）
     */
    Long applyJob(Long userId, Long jobId, JobApplyDTO dto);

    /**
     * 我投递的职位列表
     */
    PageResult<JobApplicationVO> getMyApplied(Long userId, long page, long size);

    // ==================== HR 候选人管理 ====================

    /**
     * 某职位的投递者列表（HR 视角，校验职位归属）
     */
    PageResult<CandidateVO> getJobCandidates(Long hrUserId, Long jobId, long page, long size);

    /**
     * 候选人详情（HR 视角：基本资料 + 徽章摘要 + 附带简历，校验职位归属）
     */
    CandidateDetailVO getCandidateDetail(Long hrUserId, Long appId);

    /**
     * 更新投递状态（初筛/面试/录用/拒绝 + HR备注，校验职位归属）
     */
    void updateApplicationStatus(Long hrUserId, Long appId, ApplicationStatusDTO dto);
}