package com.iwantjob.community.service.impl;

import com.iwantjob.common.enums.BadgeCondEnum;
import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.common.event.BadgeTriggerEvent;
import com.iwantjob.common.event.PointChangeEvent;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.community.dto.AnswerCreateDTO;
import com.iwantjob.community.dto.AnswerVO;
import com.iwantjob.community.entity.Answer;
import com.iwantjob.community.entity.Post;
import com.iwantjob.community.mapper.AnswerMapper;
import com.iwantjob.community.mapper.PostMapper;
import com.iwantjob.community.service.AnswerService;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回答服务实现
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final PostMapper postMapper;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public AnswerVO createAnswer(Long postId, AnswerCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        Answer answer = new Answer();
        answer.setPostId(postId);
        answer.setAuthorId(userId);
        answer.setContent(dto.getContent());
        answer.setIsAccepted(0);
        answer.setLikeCount(0);
        answerMapper.insert(answer);
        return toVO(answer);
    }

    /**
     * 采纳答案：
     * 1. 校验当前登录用户为帖子作者
     * 2. 标记答案 is_accepted=1，帖子 is_solved=1
     * 3. 发布徽章触发事件（HELP_OTHERS）与积分变动事件给回答作者
     */
    @Override
    @Transactional
    public void acceptAnswer(Long answerId) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        Post post = postMapper.selectById(answer.getPostId());
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        // 仅帖子作者可采纳
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }
        // 采纳答案
        answer.setIsAccepted(1);
        answerMapper.updateById(answer);
        // 帖子标记为已解决
        post.setIsSolved(1);
        postMapper.updateById(post);

        // 触发徽章与积分事件，奖励回答作者（帮助他人）
        publisher.publishEvent(new BadgeTriggerEvent(
                answer.getAuthorId(), BadgeCondEnum.HELP_OTHERS, answer.getId()));
        publisher.publishEvent(new PointChangeEvent(
                answer.getAuthorId(),
                PointReasonEnum.ANSWER_ACCEPTED.getDefaultPoints(),
                PointReasonEnum.ANSWER_ACCEPTED.getDesc(),
                answer.getId()));
    }

    private AnswerVO toVO(Answer answer) {
        AnswerVO vo = new AnswerVO();
        BeanUtils.copyProperties(answer, vo);
        return vo;
    }
}
