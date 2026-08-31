package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.JobCreateDTO;
import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 职位服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;

    @Override
    public PageResult<JobVO> searchJobs(JobSearchDTO param) {
        Page<Job> page = new Page<>(param.getPage(), param.getSize());
        IPage<Job> result = jobMapper.searchJobs(page, param.getKeyword(), param.getType(), param.getCity());

        List<JobVO> vos = result.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public JobVO getJobDetail(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        // 浏览数自增（乐观更新，不影响详情返回）
        jobMapper.incrementViewCount(id);
        return toVO(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishJob(Long posterId, JobCreateDTO dto) {
        Job job = new Job();
        BeanUtils.copyProperties(dto, job);
        job.setPosterId(posterId);
        job.setSource(0);   // 0-用户发布
        job.setStatus(1);   // 1-正常
        job.setViewCount(0);
        jobMapper.insert(job);
        log.info("发布职位成功: id={}, posterId={}, title={}", job.getId(), posterId, job.getTitle());
        return job.getId();
    }

    private JobVO toVO(Job job) {
        JobVO vo = new JobVO();
        BeanUtils.copyProperties(job, vo);
        return vo;
    }
}
