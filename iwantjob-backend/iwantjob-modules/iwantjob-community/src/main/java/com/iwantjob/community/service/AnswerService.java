package com.iwantjob.community.service;

import com.iwantjob.community.dto.AnswerCreateDTO;
import com.iwantjob.community.dto.AnswerVO;

/**
 * 回答服务
 */
public interface AnswerService {

    /**
     * 回答帖子
     */
    AnswerVO createAnswer(Long postId, AnswerCreateDTO dto);

    /**
     * 采纳答案（仅帖子作者可操作，触发徽章+积分事件）
     */
    void acceptAnswer(Long answerId);
}
