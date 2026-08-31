package com.iwantjob.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子 Mapper
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 浏览量自增 1（详情接口调用）
     */
    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    /**
     * FULLTEXT 检索：使用 MySQL MATCH(title, content) AGAINST(IN BOOLEAN MODE)
     * 配合 PaginationInnerInterceptor 自动分页
     */
    @Select("SELECT * FROM post WHERE is_deleted = 0 " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) " +
            "ORDER BY is_pinned DESC, created_at DESC")
    IPage<Post> searchByFulltext(IPage<Post> page, @Param("keyword") String keyword);
}
