package com.iwantjob.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.interview.ai.AiEvaluation;
import com.iwantjob.interview.ai.AiQuestion;
import com.iwantjob.interview.ai.AiReport;
import com.iwantjob.interview.ai.InterviewAiGateway;
import com.iwantjob.interview.dto.InterviewAnswerDTO;
import com.iwantjob.interview.dto.InterviewAnswerVO;
import com.iwantjob.interview.dto.InterviewDetailVO;
import com.iwantjob.interview.dto.InterviewEndVO;
import com.iwantjob.interview.dto.InterviewHistoryVO;
import com.iwantjob.interview.dto.InterviewStartDTO;
import com.iwantjob.interview.dto.InterviewStartVO;
import com.iwantjob.interview.dto.QuestionBankVO;
import com.iwantjob.interview.dto.QuestionDetailVO;
import com.iwantjob.interview.dto.QuestionVO;
import com.iwantjob.interview.dto.ScoreSummaryVO;
import com.iwantjob.interview.entity.InterviewQuestion;
import com.iwantjob.interview.entity.MockInterview;
import com.iwantjob.interview.entity.QuestionBank;
import com.iwantjob.interview.mapper.InterviewQuestionMapper;
import com.iwantjob.interview.mapper.MockInterviewMapper;
import com.iwantjob.interview.mapper.QuestionBankMapper;
import com.iwantjob.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟面试服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    /** 单场面试题目数 */
    private static final int QUESTION_COUNT = 5;
    /** 面试状态：进行中 */
    private static final int STATUS_IN_PROGRESS = 0;
    /** 面试状态：完成 */
    private static final int STATUS_COMPLETED = 1;

    private final MockInterviewMapper mockInterviewMapper;
    private final InterviewQuestionMapper interviewQuestionMapper;
    private final QuestionBankMapper questionBankMapper;
    private final InterviewAiGateway interviewAiGateway;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewStartVO start(Long userId, InterviewStartDTO dto) {
        // 1. 创建面试会话
        MockInterview mock = new MockInterview();
        mock.setUserId(userId);
        mock.setType(dto.getType());
        mock.setDifficulty(dto.getDifficulty() == null ? 1 : dto.getDifficulty());
        mock.setTargetJob(dto.getTargetJob());
        mock.setStatus(STATUS_IN_PROGRESS);
        mock.setStartTime(LocalDateTime.now());
        mockInterviewMapper.insert(mock);
        log.info("创建面试会话: id={}, userId={}, type={}", mock.getId(), userId, dto.getType());

        // 2. 从题库随机抽题
        List<QuestionBank> bank = questionBankMapper.selectRandomByCategory(dto.getType(), QUESTION_COUNT);
        if (bank == null) {
            bank = new ArrayList<>();
        }
        // 3. 题库不足时回退到 AI 网关动态生成，保证会话可用
        if (bank.isEmpty()) {
            log.warn("题库为空(type={})，回退 AI 网关生成题目", dto.getType());
            for (int i = 0; i < QUESTION_COUNT; i++) {
                AiQuestion aiQ = interviewAiGateway.generateQuestion(dto.getType(), mock.getDifficulty(), dto.getTargetJob());
                QuestionBank qb = new QuestionBank();
                qb.setCategory(dto.getType());
                qb.setQuestionText(aiQ.getQuestionText());
                qb.setExpectedKeywords(aiQ.getExpectedKeywords());
                qb.setDifficulty(mock.getDifficulty());
                bank.add(qb);
            }
        }

        // 4. 落库题目（最多 QUESTION_COUNT 道）
        int limit = Math.min(QUESTION_COUNT, bank.size());
        for (int i = 0; i < limit; i++) {
            QuestionBank qb = bank.get(i);
            InterviewQuestion iq = new InterviewQuestion();
            iq.setMockId(mock.getId());
            iq.setQuestionText(qb.getQuestionText());
            iq.setSortOrder(i + 1);
            interviewQuestionMapper.insert(iq);
        }

        // 5. 组装响应：返回第一题
        List<InterviewQuestion> questions = interviewQuestionMapper.selectByMockId(mock.getId());
        InterviewStartVO vo = new InterviewStartVO();
        vo.setMockId(mock.getId());
        vo.setType(mock.getType());
        vo.setDifficulty(mock.getDifficulty());
        vo.setTargetJob(mock.getTargetJob());
        vo.setTotalQuestions(questions.size());
        if (!questions.isEmpty()) {
            vo.setCurrentQuestion(toQuestionVO(questions.get(0)));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewAnswerVO answer(Long userId, InterviewAnswerDTO dto) {
        MockInterview mock = getOwnedInterview(userId, dto.getMockId());
        assertInProgress(mock);

        // 查询当前题目
        InterviewQuestion question = interviewQuestionMapper.selectById(dto.getQuestionId());
        if (question == null || !mock.getId().equals(question.getMockId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "问题不属于该面试会话");
        }

        // 保存回答
        question.setAnswerText(dto.getAnswerText());
        interviewQuestionMapper.updateById(question);

        // AI 评价（题库的期望关键词未冗余进 interview_question，故传 null）
        AiEvaluation eval = interviewAiGateway.evaluateAnswer(
                question.getQuestionText(), dto.getAnswerText(), null);
        question.setAiFeedback(eval.getFeedback());
        question.setReferenceAnswer(eval.getReferenceAnswer());
        interviewQuestionMapper.updateById(question);

        // 查找下一题（按 sort_order 升序的下一道未作答题目）
        List<InterviewQuestion> all = interviewQuestionMapper.selectByMockId(mock.getId());
        InterviewQuestion next = null;
        for (InterviewQuestion q : all) {
            if (q.getSortOrder() != null && q.getSortOrder() > question.getSortOrder()
                    && (q.getAnswerText() == null || q.getAnswerText().isBlank())) {
                next = q;
                break;
            }
        }

        InterviewAnswerVO vo = new InterviewAnswerVO();
        vo.setMockId(mock.getId());
        vo.setCurrentQuestionId(question.getId());
        vo.setAiFeedback(eval.getFeedback());
        vo.setReferenceAnswer(eval.getReferenceAnswer());
        vo.setHasNext(next != null);
        vo.setNextQuestion(next == null ? null : toQuestionVO(next));
        return vo;
    }

    @Override
    public PageResult<InterviewHistoryVO> history(Long userId, long page, long size) {
        Page<MockInterview> p = new Page<>(page, size);
        LambdaQueryWrapper<MockInterview> wrapper = new LambdaQueryWrapper<MockInterview>()
                .eq(MockInterview::getUserId, userId)
                .orderByDesc(MockInterview::getStartTime);
        IPage<MockInterview> result = mockInterviewMapper.selectPage(p, wrapper);

        List<InterviewHistoryVO> records = result.getRecords().stream()
                .map(this::toHistoryVO)
                .toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public InterviewDetailVO detail(Long userId, Long mockId) {
        MockInterview mock = getOwnedInterview(userId, mockId);
        List<InterviewQuestion> questions = interviewQuestionMapper.selectByMockId(mockId);

        InterviewDetailVO vo = new InterviewDetailVO();
        vo.setId(mock.getId());
        vo.setType(mock.getType());
        vo.setDifficulty(mock.getDifficulty());
        vo.setTargetJob(mock.getTargetJob());
        vo.setStatus(mock.getStatus());
        vo.setStartTime(mock.getStartTime());
        vo.setEndTime(mock.getEndTime());
        vo.setScoreSummary(parseScoreSummary(mock.getScoreSummary()));
        vo.setQuestions(questions.stream().map(this::toQuestionDetailVO).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewEndVO end(Long userId, Long mockId) {
        MockInterview mock = getOwnedInterview(userId, mockId);
        assertInProgress(mock);

        List<InterviewQuestion> questions = interviewQuestionMapper.selectByMockId(mockId);
        AiReport report = interviewAiGateway.generateReport(
                mock.getType(), mock.getDifficulty(), mock.getTargetJob(), questions);

        ScoreSummaryVO summary = toScoreSummary(report);
        mock.setScoreSummary(toJson(summary));
        mock.setStatus(STATUS_COMPLETED);
        mock.setEndTime(LocalDateTime.now());
        mockInterviewMapper.updateById(mock);
        log.info("结束面试会话: id={}, overallScore={}", mock.getId(), summary.getOverallScore());

        InterviewEndVO vo = new InterviewEndVO();
        vo.setMockId(mock.getId());
        vo.setStatus(mock.getStatus());
        vo.setScoreSummary(summary);
        vo.setEndTime(mock.getEndTime());
        return vo;
    }

    // ==================== 内部工具方法 ====================

    /**
     * 获取面试会话并校验归属权（只能操作自己的面试）
     */
    private MockInterview getOwnedInterview(Long userId, Long mockId) {
        MockInterview mock = mockInterviewMapper.selectById(mockId);
        if (mock == null) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND);
        }
        if (!userId.equals(mock.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作他人的面试会话");
        }
        return mock;
    }

    /**
     * 断言面试进行中
     */
    private void assertInProgress(MockInterview mock) {
        if (mock.getStatus() != null && mock.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INTERVIEW_ENDED);
        }
    }

    private QuestionVO toQuestionVO(InterviewQuestion q) {
        QuestionVO vo = new QuestionVO();
        vo.setQuestionId(q.getId());
        vo.setSortOrder(q.getSortOrder());
        vo.setQuestionText(q.getQuestionText());
        return vo;
    }

    private QuestionDetailVO toQuestionDetailVO(InterviewQuestion q) {
        QuestionDetailVO vo = new QuestionDetailVO();
        BeanUtils.copyProperties(q, vo);
        return vo;
    }

    private InterviewHistoryVO toHistoryVO(MockInterview mock) {
        InterviewHistoryVO vo = new InterviewHistoryVO();
        vo.setId(mock.getId());
        vo.setType(mock.getType());
        vo.setDifficulty(mock.getDifficulty());
        vo.setTargetJob(mock.getTargetJob());
        vo.setStatus(mock.getStatus());
        vo.setScoreSummary(mock.getScoreSummary());
        vo.setStartTime(mock.getStartTime());
        vo.setEndTime(mock.getEndTime());
        return vo;
    }

    private ScoreSummaryVO toScoreSummary(AiReport report) {
        ScoreSummaryVO vo = new ScoreSummaryVO();
        vo.setOverallScore(report.getOverallScore());
        vo.setDimensionScores(report.getDimensionScores());
        vo.setSummary(report.getSummary());
        vo.setSuggestions(report.getSuggestions());
        return vo;
    }

    private ScoreSummaryVO parseScoreSummary(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ScoreSummaryVO.class);
        } catch (JsonProcessingException e) {
            log.warn("解析 score_summary 失败: {}", e.getMessage());
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化 score_summary 失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评分序列化失败");
        }
    }

    // ==================== 题库浏览（学生学习中心） ====================

    @Override
    public PageResult<QuestionBankVO> listQuestions(long page, long size, Integer category, String subCategory) {
        Page<QuestionBank> p = new Page<>(page, size);
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<QuestionBank>()
                .eq(category != null, QuestionBank::getCategory, category)
                .eq(subCategory != null && !subCategory.isBlank(), QuestionBank::getSubCategory, subCategory)
                .orderByAsc(QuestionBank::getCategory)
                .orderByAsc(QuestionBank::getId);
        IPage<QuestionBank> result = questionBankMapper.selectPage(p, wrapper);
        List<QuestionBankVO> vos = result.getRecords().stream()
                .map(this::toBankVO)
                .toList();
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public QuestionBankVO questionDetail(Long id) {
        QuestionBank qb = questionBankMapper.selectById(id);
        if (qb == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
        return toBankVO(qb);
    }

    private QuestionBankVO toBankVO(QuestionBank qb) {
        QuestionBankVO vo = new QuestionBankVO();
        vo.setId(qb.getId());
        vo.setCategory(qb.getCategory());
        vo.setSubCategory(qb.getSubCategory());
        vo.setQuestionText(qb.getQuestionText());
        vo.setDifficulty(qb.getDifficulty());
        vo.setExpectedKeywords(qb.getExpectedKeywords());
        return vo;
    }}
