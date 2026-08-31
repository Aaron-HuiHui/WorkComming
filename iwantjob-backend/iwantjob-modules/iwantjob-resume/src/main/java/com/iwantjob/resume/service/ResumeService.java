package com.iwantjob.resume.service;

import com.iwantjob.resume.dto.ResumeCreateDTO;
import com.iwantjob.resume.dto.ResumeMatchVO;
import com.iwantjob.resume.dto.ResumeOptimizeDTO;
import com.iwantjob.resume.dto.ResumeOptimizeVO;
import com.iwantjob.resume.dto.ResumeScoreVO;
import com.iwantjob.resume.dto.ResumeUpdateDTO;
import com.iwantjob.resume.dto.ResumeVO;

import java.util.List;

/**
 * 简历服务接口
 */
public interface ResumeService {

    /**
     * 创建简历
     */
    Long createResume(Long userId, ResumeCreateDTO dto);

    /**
     * 我的简历列表
     */
    List<ResumeVO> listMyResumes(Long userId);

    /**
     * 简历详情（校验归属）
     */
    ResumeVO getResumeDetail(Long userId, Long id);

    /**
     * 更新简历（校验归属）
     */
    void updateResume(Long userId, Long id, ResumeUpdateDTO dto);

    /**
     * 删除简历（软删除，校验归属）
     */
    void deleteResume(Long userId, Long id);

    /**
     * AI 简历优化（写 optimization_log）
     */
    ResumeOptimizeVO optimizeResume(Long userId, ResumeOptimizeDTO dto);

    /**
     * AI 简历评分（更新 resume.ai_score）
     */
    ResumeScoreVO scoreResume(Long userId, Long resumeId);

    /**
     * 简历-职位匹配度（关键词重叠率）
     */
    ResumeMatchVO matchJob(Long userId, Long resumeId, Long jobId);
}
