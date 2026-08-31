package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            "ORDER BY created_at DESC",
            "</script>"
    })
    IPage<Job> searchJobs(IPage<Job> page,
                          @Param("keyword") String keyword,
                          @Param("type") Integer type,
                          @Param("city") String city);

    /**
     * 浏览数自增（乐观更新，避免读改写覆盖）
     */
    @Update("UPDATE job SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementViewCount(@Param("id") Long id);
}
