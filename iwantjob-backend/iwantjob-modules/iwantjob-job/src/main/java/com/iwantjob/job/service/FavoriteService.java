package com.iwantjob.job.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.JobVO;

import java.util.List;

/**
 * 职位收藏服务接口
 */
public interface FavoriteService {

    /** 收藏/取消收藏切换，返回收藏状态 */
    Boolean toggleFavorite(Long userId, Long jobId);

    /** 我的收藏职位列表 */
    PageResult<JobVO> getMyFavorites(Long userId, long page, long size);

    /** 我收藏的职位ID集合（前端标记星标态） */
    List<Long> getFavoriteIds(Long userId);
}