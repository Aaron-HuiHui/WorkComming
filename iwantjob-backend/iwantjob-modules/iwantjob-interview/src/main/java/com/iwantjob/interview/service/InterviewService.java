package com.iwantjob.interview.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.interview.dto.InterviewAnswerDTO;
import com.iwantjob.interview.dto.InterviewAnswerVO;
import com.iwantjob.interview.dto.InterviewDetailVO;
import com.iwantjob.interview.dto.InterviewEndVO;
import com.iwantjob.interview.dto.InterviewHistoryVO;
import com.iwantjob.interview.dto.InterviewStartDTO;
import com.iwantjob.interview.dto.InterviewStartVO;

/**
 * 模拟面试服务
 */
public interface InterviewService {

    /**
     * 创建面试会话：从题库随机抽取5题，返回第一题
     */
    InterviewStartVO start(Long userId, InterviewStartDTO dto);

    /**
     * 提交回答：AI 反馈 + 返回下一题
     */
    InterviewAnswerVO answer(Long userId, InterviewAnswerDTO dto);

    /**
     * 面试历史分页（仅本人）
     */
    PageResult<InterviewHistoryVO> history(Long userId, long page, long size);

    /**
     * 面试详情（含全部题目）
     */
    InterviewDetailVO detail(Long userId, Long mockId);

    /**
     * 结束面试：生成评分汇总
     */
    InterviewEndVO end(Long userId, Long mockId);
}
