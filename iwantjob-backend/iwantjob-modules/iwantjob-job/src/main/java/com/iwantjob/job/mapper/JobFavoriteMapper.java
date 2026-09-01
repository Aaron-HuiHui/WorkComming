package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.entity.JobFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 职位收藏 Mapper
 */
@Mapper
public interface JobFavoriteMapper extends BaseMapper<JobFavorite> {

    /**
     * 我的收藏列表（联表职位摘要）
     */
    @Select({
            "SELECT j.id, j.title, j.company_name AS companyName, j.company_id AS companyId,",
            "       j.job_type AS jobType, j.recruitment_batch AS recruitmentBatch,",
            "       j.salary_range AS salaryRange, j.location, j.description, j.requirements,",
            "       j.source, j.contact_email AS contactEmail, j.expiry_date AS expiryDate,",
            "       j.view_count AS viewCount, j.poster_id AS posterId, j.status, j.created_at AS createdAt",
            "FROM job_favorite f",
            "INNER JOIN job j ON j.id = f.job_id AND j.is_deleted = 0",
            "WHERE f.user_id = #{userId}",
            "ORDER BY f.created_at DESC"
    })
    IPage<JobVO> selectMyFavorites(IPage<JobVO> page, @Param("userId") Long userId);
}