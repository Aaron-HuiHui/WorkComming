package com.iwantjob.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.community.dto.PostCreateDTO;
import com.iwantjob.community.dto.PostDetailVO;
import com.iwantjob.community.dto.PostListVO;
import com.iwantjob.community.dto.PostQueryDTO;
import com.iwantjob.community.entity.Post;
import com.iwantjob.community.mapper.PostMapper;
import com.iwantjob.community.service.PostService;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    @Transactional
    public PostDetailVO createPost(PostCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Post post = new Post();
        post.setAuthorId(userId);
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setTags(dto.getTags());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setIsPinned(0);
        post.setIsSolved(0);
        postMapper.insert(post);
        return toDetailVO(post);
    }

    @Override
    public PageResult<PostListVO> pagePosts(PostQueryDTO query) {
        Page<Post> page = new Page<>(query.getPage(), query.getSize());
        IPage<Post> result;
        String keyword = query.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            // 走 FULLTEXT 索引 ft_post_search(title, content)
            result = postMapper.searchByFulltext(page, keyword.trim());
        } else {
            LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                    .eq(query.getType() != null, Post::getType, query.getType())
                    .orderByDesc(Post::getIsPinned)
                    .orderByDesc(Post::getCreatedAt);
            result = postMapper.selectPage(page, wrapper);
        }
        List<PostListVO> records = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PostDetailVO getPostDetail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        // 浏览量自增
        postMapper.incrementViewCount(id);
        // 返回时反映最新浏览量（已 +1）
        int currentViews = (post.getViewCount() == null ? 0 : post.getViewCount()) + 1;
        post.setViewCount(currentViews);
        return toDetailVO(post);
    }

    private PostDetailVO toDetailVO(Post post) {
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }

    private PostListVO toListVO(Post post) {
        PostListVO vo = new PostListVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
