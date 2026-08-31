package com.iwantjob.resume.service.impl;

import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.resume.dto.ResumeCreateDTO;
import com.iwantjob.resume.dto.ResumeMatchVO;
import com.iwantjob.resume.dto.ResumeOptimizeDTO;
import com.iwantjob.resume.dto.ResumeOptimizeVO;
import com.iwantjob.resume.dto.ResumeScoreVO;
import com.iwantjob.resume.dto.ResumeUpdateDTO;
import com.iwantjob.resume.dto.ResumeVO;
import com.iwantjob.resume.entity.Resume;
import com.iwantjob.resume.entity.ResumeOptimizationLog;
import com.iwantjob.resume.mapper.ResumeMapper;
import com.iwantjob.resume.mapper.ResumeOptimizationLogMapper;
import com.iwantjob.resume.service.ResumeAiGateway;
import com.iwantjob.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历服务实现
 * 关键点：
 *  - 所有写/读/删操作均校验 user_id 归属，越权抛 FORBIDDEN
 *  - content_json 以 String 承载，应用层自行解析
 *  - AI 调用通过 ResumeAiGateway 解耦，默认 Mock 实现
 *  - 匹配度：中英文关键词重叠率（中文 2-gram + 英文词）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final ResumeOptimizationLogMapper optimizationLogMapper;
    private final ResumeAiGateway resumeAiGateway;

    /** 英文/技术词提取：字母数字开头，含 + # . 的词（如 Java、C++、SpringBoot、node.js） */
    private static final Pattern EN_TOKEN = Pattern.compile("[A-Za-z][A-Za-z0-9+#.]{1,30}");
    /** 中文 2-gram 提取 */
    private static final Pattern CN_PAIR = Pattern.compile("[\\u4e00-\\u9fa5]{2}");
    /** 通用停用词（中文 2-gram 噪声过滤） */
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "我们", "你们", "他们", "自己", "这是", "那是", "一个", "一些", "可以", "能够",
            "进行", "以及", "并且", "但是", "因为", "所以", "如果", "虽然", "比如", "例如",
            "通过", "使用", "实现", "完成", "具有", "属于", "处于", "作为", "对于", "关于",
            "已经", "正在", "需要", "应该", "可能", "或者", "以及", "以上", "以下", "以内",
            "之后", "之前", "之间", "其中", "其他", "其它", "本次", "本次", "此次", "该等"
    ));

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResume(Long userId, ResumeCreateDTO dto) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(dto.getTitle());
        resume.setContentJson(dto.getContentJson());
        resume.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() == 1 ? 1 : 0);
        resume.setVersion(1);
        resumeMapper.insert(resume);

        // 若设为默认，清空其他默认
        if (resume.getIsDefault() == 1) {
            resumeMapper.clearDefaultForUser(userId);
            // 重新置本条为默认（clearDefault 会把所有置 0，再 update 回 1）
            Resume upd = new Resume();
            upd.setId(resume.getId());
            upd.setIsDefault(1);
            resumeMapper.updateById(upd);
        }
        log.info("创建简历成功: id={}, userId={}, title={}", resume.getId(), userId, resume.getTitle());
        return resume.getId();
    }

    @Override
    public List<ResumeVO> listMyResumes(Long userId) {
        List<Resume> list = resumeMapper.selectMyList(userId);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public ResumeVO getResumeDetail(Long userId, Long id) {
        Resume resume = requireOwnedResume(userId, id);
        return toVO(resume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResume(Long userId, Long id, ResumeUpdateDTO dto) {
        Resume resume = requireOwnedResume(userId, id);
        boolean changed = false;
        Resume upd = new Resume();
        upd.setId(id);
        if (dto.getTitle() != null && !dto.getTitle().equals(resume.getTitle())) {
            upd.setTitle(dto.getTitle());
            changed = true;
        }
        if (dto.getContentJson() != null && !dto.getContentJson().equals(resume.getContentJson())) {
            upd.setContentJson(dto.getContentJson());
            changed = true;
        }
        if (dto.getIsDefault() != null) {
            int target = dto.getIsDefault() == 1 ? 1 : 0;
            if (target == 1 && (resume.getIsDefault() == null || resume.getIsDefault() != 1)) {
                // 设为默认：先清空其他，再置本条
                resumeMapper.clearDefaultForUser(userId);
                upd.setIsDefault(1);
                changed = true;
            } else if (target == 0 && (resume.getIsDefault() != null && resume.getIsDefault() == 1)) {
                upd.setIsDefault(0);
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        // 清空 AI 评分（内容变更后旧评分失效）
        upd.setAiScore(null);
        resumeMapper.updateById(upd);
        resumeMapper.incrementVersion(id);
        log.info("更新简历成功: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long userId, Long id) {
        requireOwnedResume(userId, id);
        resumeMapper.deleteById(id);
        log.info("删除简历(软删)成功: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeOptimizeVO optimizeResume(Long userId, ResumeOptimizeDTO dto) {
        Resume resume = requireOwnedResume(userId, dto.getResumeId());
        String original = extractPlainText(resume.getContentJson());
        String optimized = resumeAiGateway.optimize(original, dto.getType(), dto.getTargetLang());

        // 写优化日志
        ResumeOptimizationLog log = new ResumeOptimizationLog();
        log.setResumeId(resume.getId());
        log.setUserId(userId);
        log.setOriginalText(original);
        log.setOptimizedText(optimized);
        log.setType(dto.getType());
        log.setFeedback("Mock 优化反馈：已按类型 " + dto.getType() + " 处理，可结合 AiChatService 真实模型回填。");
        optimizationLogMapper.insert(log);

        ResumeOptimizeVO vo = new ResumeOptimizeVO();
        vo.setResumeId(resume.getId());
        vo.setType(dto.getType());
        vo.setOriginalText(original);
        vo.setOptimizedText(optimized);
        vo.setFeedback(log.getFeedback());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeScoreVO scoreResume(Long userId, Long resumeId) {
        Resume resume = requireOwnedResume(userId, resumeId);
        String content = extractPlainText(resume.getContentJson());
        Integer score = resumeAiGateway.score(content);
        if (score == null) {
            score = 0;
        }
        // 钳制 0-100
        score = Math.max(0, Math.min(100, score));
        resumeMapper.updateAiScore(resumeId, score);

        ResumeScoreVO vo = new ResumeScoreVO();
        vo.setResumeId(resumeId);
        vo.setAiScore(score);
        vo.setFeedback("Mock 评分：基于内容长度估算 " + score + "/100。接入真实模型后会综合考量结构、关键词、量化成果等。");
        return vo;
    }

    @Override
    public ResumeMatchVO matchJob(Long userId, Long resumeId, Long jobId) {
        Resume resume = requireOwnedResume(userId, resumeId);
        Map<String, Object> jobRow = resumeMapper.selectJobForMatch(jobId);
        if (jobRow == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        String resumeText = extractPlainText(resume.getContentJson());
        String jobText = joinJobText(jobRow);

        Set<String> resumeKw = extractKeywords(resumeText);
        Set<String> jobKw = extractKeywords(jobText);

        // 计算交集
        LinkedHashSet<String> matched = new LinkedHashSet<>();
        for (String kw : jobKw) {
            if (resumeKw.contains(kw)) {
                matched.add(kw);
            }
        }
        int denom = Math.min(resumeKw.size(), jobKw.size());
        int matchScore = denom == 0 ? 0 : (int) Math.round(matched.size() * 100.0 / denom);

        ResumeMatchVO vo = new ResumeMatchVO();
        vo.setResumeId(resumeId);
        vo.setJobId(jobId);
        vo.setMatchScore(matchScore);
        vo.setMatchedKeywords(new ArrayList<>(matched));
        vo.setResumeKeywordCount(resumeKw.size());
        vo.setJobKeywordCount(jobKw.size());
        return vo;
    }

    // ===================== private helpers =====================

    /**
     * 校验简历存在且属于当前用户
     */
    private Resume requireOwnedResume(Long userId, Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
        }
        if (!userId.equals(resume.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作他人简历");
        }
        return resume;
    }

    private ResumeVO toVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo);
        return vo;
    }

    /**
     * 从 content_json 字符串提取纯文本（去 JSON 结构符号与字段名）
     * 简化策略：保留 "..." 内的字符串值，拼接为文本
     */
    private String extractPlainText(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 提取所有双引号内的字符串值（去除 JSON key 与符号噪声）
        Matcher m = Pattern.compile("\"([^\"]+)\"").matcher(contentJson);
        while (m.find()) {
            String s = m.group(1);
            // 跳过纯数字/布尔/null 这类无效关键词
            if (s.isBlank() || "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)
                    || "null".equalsIgnoreCase(s)) {
                continue;
            }
            sb.append(s).append(' ');
        }
        return sb.toString();
    }

    private String joinJobText(Map<String, Object> jobRow) {
        StringBuilder sb = new StringBuilder();
        appendVal(sb, jobRow.get("jobTitle"));
        appendVal(sb, jobRow.get("jobDescription"));
        appendVal(sb, jobRow.get("jobRequirements"));
        return sb.toString();
    }

    private void appendVal(StringBuilder sb, Object v) {
        if (v != null) {
            sb.append(v.toString()).append(' ');
        }
    }

    /**
     * 关键词提取：英文技术词 + 中文 2-gram，去停用词
     */
    private Set<String> extractKeywords(String text) {
        Set<String> kws = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return kws;
        }
        // 英文/技术词（小写归一）
        Matcher en = EN_TOKEN.matcher(text);
        while (en.find()) {
            String w = en.group().toLowerCase();
            if (w.length() >= 2) {
                kws.add(w);
            }
        }
        // 中文 2-gram
        Matcher cn = CN_PAIR.matcher(text);
        while (cn.find()) {
            String pair = cn.group();
            if (!STOPWORDS.contains(pair)) {
                kws.add(pair);
            }
        }
        return kws;
    }
}
