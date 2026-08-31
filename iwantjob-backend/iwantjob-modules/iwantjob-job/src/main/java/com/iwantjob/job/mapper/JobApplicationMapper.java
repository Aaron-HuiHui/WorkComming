package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 职位投递 Mapper
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 我投递的职位列表（联表 job 取职位摘要）
     */
    @Select({
            "<script>",
            "SELECT a.id, a.job_id AS jobId, a.user_id AS userId, a.resume_id AS resumeId,",
            "       a.cover_letter AS coverLetter, a.status, a.hr_remark AS hrRemark, a.applied_at AS appliedAt,",
            "       j.title AS jobTitle, j.company_name AS companyName, j.job_type AS jobType,",
            "       j.location AS location, j.salary_range AS salaryRange",
            "FROM job_application a",
            "INNER JOIN job j ON j.id = a.job_id AND j.is_deleted = 0",
            "WHERE a.is_deleted = 0 AND a.user_id = #{userId}",
            "ORDER BY a.applied_at DESC",
            "</script>"
    })
    IPage<JobApplicationVO> selectMyApplied(IPage<JobApplicationVO> page, @Param("userId") Long userId);
}
