package com.iwantjob.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.resume.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 简历 Mapper
 * - selectMyList: 当前用户的简历列表（无分页，简历数量通常 < 20）
 * - selectJobForMatch: 跨表查询 job 的 title/description/requirements（仅 SQL，不依赖 job 模块）
 * - incrementVersion: 乐观版本自增
 */
@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {

    /**
     * 当前用户的简历列表（按 is_default 优先、created_at 倒序）
     */
    @Select({
            "<script>",
            "SELECT * FROM resume",
            "WHERE is_deleted = 0 AND user_id = #{userId}",
            "ORDER BY is_default DESC, created_at DESC",
            "</script>"
    })
    List<Resume> selectMyList(@Param("userId") Long userId);

    /**
     * 简历-职位匹配度：跨表取 job 的关键字段（避免跨模块 import Job 实体）
     * 返回一行业务字段，由 service 解析
     */
    @Select({
            "<script>",
            "SELECT j.title AS jobTitle, j.description AS jobDescription, j.requirements AS jobRequirements",
            "FROM job j",
            "WHERE j.is_deleted = 0 AND j.id = #{jobId}",
            "</script>"
    })
    java.util.Map<String, Object> selectJobForMatch(@Param("jobId") Long jobId);

    /**
     * 版本号自增（更新简历时使用，避免读改写覆盖）
     */
    @Update("UPDATE resume SET version = version + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementVersion(@Param("id") Long id);

    /**
     * 更新 AI 评分
     */
    @Update("UPDATE resume SET ai_score = #{score} WHERE id = #{id} AND is_deleted = 0")
    int updateAiScore(@Param("id") Long id, @Param("score") Integer score);

    /**
     * 将其他简历的 is_default 置 0（设置默认简历时调用）
     */
    @Update("UPDATE resume SET is_default = 0 WHERE user_id = #{userId} AND is_deleted = 0")
    int clearDefaultForUser(@Param("userId") Long userId);
}
