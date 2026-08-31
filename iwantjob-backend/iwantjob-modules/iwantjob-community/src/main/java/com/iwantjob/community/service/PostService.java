package com.iwantjob.community.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.community.dto.PostCreateDTO;
import com.iwantjob.community.dto.PostDetailVO;
import com.iwantjob.community.dto.PostListVO;
import com.iwantjob.community.dto.PostQueryDTO;

/**
 * 帖子服务
 */
public interface PostService {

    /**
     * 发帖
     */
    PostDetailVO createPost(PostCreateDTO dto);

    /**
     * 分页列表（支持 FULLTEXT 搜索）
     */
    PageResult<PostListVO> pagePosts(PostQueryDTO query);

    /**
     * 帖子详情（view_count + 1）
     */
    PostDetailVO getPostDetail(Long id);
}
