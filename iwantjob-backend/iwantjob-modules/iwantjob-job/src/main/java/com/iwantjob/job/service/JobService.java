package com.iwantjob.job.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.HrJobVO;
import com.iwantjob.job.dto.JobCreateDTO;
import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.dto.JobStatsVO;
import com.iwantjob.job.dto.JobVO;

/**
 * 职位服务接口
 */
public interface JobService {

    /**
     * 职位搜索（FULLTEXT 兜底 + 条件过滤 + 分页）
     */
    PageResult<JobVO> searchJobs(JobSearchDTO param);

    /**
     * 职位详情（view_count + 1）
     */
    JobVO getJobDetail(Long id);

    /**
     * 发布职位（poster_id = 当前用户）
     */
    Long publishJob(Long posterId, JobCreateDTO dto);

    /**
     * 我发布的职位列表（含投递数统计，HR 工作台）
     */
    PageResult<HrJobVO> getMyPublishedJobs(Long posterId, long page, long size);

    /**
     * 岗位市场统计总览（学生可视化）
     */
    JobStatsVO getStatsOverview();
}