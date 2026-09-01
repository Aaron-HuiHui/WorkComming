package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.entity.JobFavorite;
import com.iwantjob.job.mapper.JobFavoriteMapper;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 职位收藏服务实现（物理删除 + unique 防重复）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final JobFavoriteMapper jobFavoriteMapper;
    private final JobMapper jobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFavorite(Long userId, Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        JobFavorite existing = jobFavoriteMapper.selectOne(
                new LambdaQueryWrapper<JobFavorite>()
                        .eq(JobFavorite::getUserId, userId)
                        .eq(JobFavorite::getJobId, jobId));
        if (existing != null) {
            jobFavoriteMapper.deleteById(existing.getId());
            log.info("取消收藏: userId={}, jobId={}", userId, jobId);
            return false;
        }
        JobFavorite fav = new JobFavorite();
        fav.setUserId(userId);
        fav.setJobId(jobId);
        jobFavoriteMapper.insert(fav);
        log.info("收藏职位: userId={}, jobId={}", userId, jobId);
        return true;
    }

    @Override
    public PageResult<JobVO> getMyFavorites(Long userId, long page, long size) {
        Page<JobVO> p = new Page<>(page, size);
        IPage<JobVO> result = jobFavoriteMapper.selectMyFavorites(p, userId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<Long> getFavoriteIds(Long userId) {
        return jobFavoriteMapper.selectList(
                        new LambdaQueryWrapper<JobFavorite>().eq(JobFavorite::getUserId, userId))
                .stream().map(JobFavorite::getJobId).toList();
    }
}