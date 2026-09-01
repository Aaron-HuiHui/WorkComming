package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.job.dto.HrJobVO;
import com.iwantjob.job.dto.NameValueVO;
import com.iwantjob.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 职位 Mapper
 * 搜索使用 MySQL FULLTEXT (MATCH...AGAINST) 兜底，无 keyword 时退化为普通条件查询
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {

    /**
     * 职位搜索：
     *  - 有关键词：走 FULLTEXT 索引 ft_job_search(title, description, requirements)
     *  - 无关键词：退化为普通条件查询
     *  - 兼容 type / city 过滤
     * 分页由 MyBatis-Plus PaginationInnerInterceptor 自动注入 limit 与 count
     */
    @Select({
            "<script>",
            "SELECT * FROM job",
            "WHERE is_deleted = 0",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND MATCH(title, description, requirements) AGAINST(#{keyword} IN BOOLEAN MODE)",
            "</if>",
            "<if test='type != null'>",
            "  AND job_type = #{type}",
            "</if>",
            "<if test='city != null and city != \"\"'>",
            "  AND location = #{city}",
            "</if>",
            "<if test='batch != null'>",
            "  AND recruitment_batch = #{batch}",
            "</if>",
            "<if test='companyId != null'>",
            "  AND company_id = #{companyId}",
            "</if>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    IPage<Job> searchJobs(IPage<Job> page,
                          @Param("keyword") String keyword,
                          @Param("type") Integer type,
                          @Param("city") String city,
                          @Param("batch") Integer batch,
                          @Param("companyId") Long companyId);

    /**
     * 浏览数自增（乐观更新，避免读改写覆盖）
     */
    @Update("UPDATE job SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 我发布的职位列表（HR 工作台，含每个职位的投递数）
     */
    @Select({
            "SELECT j.id, j.title, j.company_name AS companyName, j.job_type AS jobType,",
            "       j.salary_range AS salaryRange, j.location, j.status, j.view_count AS viewCount,",
            "       j.created_at AS createdAt,",
            "       (SELECT COUNT(*) FROM job_application a WHERE a.job_id = j.id AND a.is_deleted = 0) AS applicationCount",
            "FROM job j",
            "WHERE j.is_deleted = 0 AND j.poster_id = #{posterId}",
            "ORDER BY j.created_at DESC"
    })
    IPage<HrJobVO> selectMyPublished(IPage<HrJobVO> page, @Param("posterId") Long posterId);

    // ==================== 岗位市场统计（学生可视化） ====================

    /**
     * 按城市分布
     */
    @Select("SELECT location AS name, COUNT(*) AS value FROM job WHERE is_deleted = 0 AND status = 1 AND location IS NOT NULL AND location != '' GROUP BY location ORDER BY value DESC")
    List<NameValueVO> statsByCity();

    /**
     * 按职位类型分布
     */
    @Select("SELECT job_type AS name, COUNT(*) AS value FROM job WHERE is_deleted = 0 AND status = 1 GROUP BY job_type")
    List<NameValueVO> statsByType();

    /**
     * 按薪资段分布（原始 salary_range 分组，段位归类在 Service 层完成）
     */
    @Select("SELECT salary_range AS name, COUNT(*) AS value FROM job WHERE is_deleted = 0 AND status = 1 AND salary_range IS NOT NULL AND salary_range != '' GROUP BY salary_range")
    List<NameValueVO> statsBySalaryRaw();

    /**
     * 浏览量 TOP 职位
     */
    @Select("SELECT CONCAT(title, ' · ', company_name) AS name, view_count AS value FROM job WHERE is_deleted = 0 AND status = 1 ORDER BY view_count DESC LIMIT #{limit}")
    List<NameValueVO> statsHotJobs(@Param("limit") int limit);

    /**
     * 在招职位总数
     */
    @Select("SELECT COUNT(*) FROM job WHERE is_deleted = 0 AND status = 1")
    Long statsTotalJobs();

    /**
     * 按招聘批次分布
     */
    @Select("SELECT recruitment_batch AS name, COUNT(*) AS value FROM job WHERE is_deleted = 0 AND status = 1 GROUP BY recruitment_batch")
    List<NameValueVO> statsByBatch();

    /**
     * 某企业在招职位数
     */
    @Select("SELECT COUNT(*) FROM job WHERE is_deleted = 0 AND status = 1 AND company_id = #{companyId}")
    Long countJobsByCompanyId(@Param("companyId") Long companyId);

    /**
     * 参与发布的企业数
     */
    @Select("SELECT COUNT(DISTINCT company_name) FROM job WHERE is_deleted = 0 AND status = 1")
    Long statsTotalCompanies();
}