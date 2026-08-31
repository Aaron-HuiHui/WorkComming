package com.iwantjob.community.controller;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.common.result.Result;
import com.iwantjob.community.dto.AnswerCreateDTO;
import com.iwantjob.community.dto.AnswerVO;
import com.iwantjob.community.dto.PostCreateDTO;
import com.iwantjob.community.dto.PostDetailVO;
import com.iwantjob.community.dto.PostListVO;
import com.iwantjob.community.dto.PostQueryDTO;
import com.iwantjob.community.service.AnswerService;
import com.iwantjob.community.service.PostService;
import com.iwantjob.framework.idempotent.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子接口
 */
@Tag(name = "社区服务", description = "帖子、回答、技能交换、众筹")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AnswerService answerService;

    @Operation(summary = "发帖")
    @PostMapping
    @Idempotent
    public Result<PostDetailVO> createPost(@RequestBody @Valid PostCreateDTO dto) {
        return Result.success(postService.createPost(dto));
    }

    @Operation(summary = "帖子分页列表（支持 FULLTEXT 搜索）")
    @GetMapping
    public Result<PageResult<PostListVO>> pagePosts(@Valid PostQueryDTO query) {
        return Result.success(postService.pagePosts(query));
    }

    @Operation(summary = "帖子详情（view_count + 1）")
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id) {
        return Result.success(postService.getPostDetail(id));
    }

    @Operation(summary = "回答帖子")
    @PostMapping("/{id}/answer")
    @Idempotent
    public Result<AnswerVO> createAnswer(@PathVariable Long id,
                                         @RequestBody @Valid AnswerCreateDTO dto) {
        return Result.success(answerService.createAnswer(id, dto));
    }
}
