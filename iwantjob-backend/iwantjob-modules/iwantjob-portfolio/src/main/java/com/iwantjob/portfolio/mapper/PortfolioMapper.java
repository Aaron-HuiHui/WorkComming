package com.iwantjob.portfolio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.portfolio.dto.PortfolioVO;
import com.iwantjob.portfolio.entity.Portfolio;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 作品集 Mapper
 */
@Mapper
public interface PortfolioMapper extends BaseMapper<Portfolio> {

    /**
     * 分页查询（联表作者信息 + 当前用户是否已点赞）
     * authorId 非空时查指定作者的作品（我的作品）
     */
    @Select({
            "<script>",
            "SELECT p.id, p.user_id AS userId, u.username AS authorName, u.real_name AS authorRealName,",
            "       p.title, p.description, p.cover, p.repo_url AS repoUrl, p.demo_url AS demoUrl,",
            "       p.tech_tags AS techTags, p.view_count AS viewCount, p.like_count AS likeCount, p.created_at AS createdAt,",
            "       (SELECT COUNT(*) FROM portfolio_like l WHERE l.portfolio_id = p.id AND l.user_id = #{currentUserId}) AS liked",
            "FROM portfolio p",
            "INNER JOIN sys_user u ON u.id = p.user_id AND u.is_deleted = 0",
            "WHERE p.is_deleted = 0",
            "<if test='tag != null and tag != \"\"'>",
            "  AND p.tech_tags LIKE CONCAT('%', #{tag}, '%')",
            "</if>",
            "<if test='authorId != null'>",
            "  AND p.user_id = #{authorId}",
            "</if>",
            "ORDER BY p.created_at DESC",
            "</script>"
    })
    IPage<PortfolioVO> selectPortfolioPage(IPage<PortfolioVO> page,
                                           @Param("currentUserId") Long currentUserId,
                                           @Param("tag") String tag,
                                           @Param("authorId") Long authorId);

    /**
     * 浏览量自增
     */
    @Update("UPDATE portfolio SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementViewCount(@Param("id") Long id);
}