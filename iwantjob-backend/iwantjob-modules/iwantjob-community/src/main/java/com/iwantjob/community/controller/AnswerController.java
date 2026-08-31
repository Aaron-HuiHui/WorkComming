package com.iwantjob.community.controller;

import com.iwantjob.common.result.Result;
import com.iwantjob.community.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回答接口
 */
@Tag(name = "社区服务", description = "帖子、回答、技能交换、众筹")
@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "采纳答案（仅帖子作者可操作，触发徽章+积分事件）")
    @PutMapping("/{id}/accept")
    public Result<Void> acceptAnswer(@PathVariable Long id) {
        answerService.acceptAnswer(id);
        return Result.success();
    }
}
