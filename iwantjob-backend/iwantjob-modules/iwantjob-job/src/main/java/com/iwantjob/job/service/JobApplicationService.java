package com.iwantjob.job.service;

import com.iwantjob.common.result.PageResult;
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
}
