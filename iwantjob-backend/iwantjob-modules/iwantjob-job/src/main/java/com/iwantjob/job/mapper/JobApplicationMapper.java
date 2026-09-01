package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwantjob.framework.datascope.DataScope;
import com.iwantjob.framework.datascope.ScopeType;
import com.iwantjob.job.dto.CandidateDetailVO;
import com.iwantjob.job.dto.CandidateVO;
import com.iwantjob.job.dto.JobApplicationVO;
import com.iwantjob.job.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 职位投递 Mapper
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 我投递的职位列表（联表 job 取职位摘要）
     * <p>
     * 标注 {@code @DataScope(SELF)}：拦截器强制追加 a.user_id = 当前用户，
     * 即使 Service 层漏传 userId 也无法越权查看他人投递（管理员除外）。
     */
    @DataScope(value = ScopeType.SELF, column = "a.user_id")
    @Select({
            "<script>",
            "SELECT a.id, a.job_id AS jobId, a.user_id AS userId, a.resume_id AS resumeId,",
            "       a.cover_letter AS coverLetter, a.status, a.hr_remark AS hrRemark, a.applied_at AS appliedAt,",
            "       a.interview_time AS interviewTime, a.interview_location AS interviewLocation, a.interview_note AS interviewNote,",
            "       j.title AS jobTitle, j.company_name AS companyName, j.job_type AS jobType,",
            "       j.location AS location, j.salary_range AS salaryRange",
            "FROM job_application a",
            "INNER JOIN job j ON j.id = a.job_id AND j.is_deleted = 0",
            "WHERE a.is_deleted = 0 AND a.user_id = #{userId}",
            "ORDER BY a.applied_at DESC",
            "</script>"
    })
    IPage<JobApplicationVO> selectMyApplied(IPage<JobApplicationVO> page, @Param("userId") Long userId);

    /**
     * 某职位的投递者列表（HR 视角，联表 sys_user/user_profile 取求职者摘要）
     * SQL 中 j.poster_id = #{posterId} 保证 HR 只能看到自己职位的投递（与 Service 校验双保险）
     */
    @Select({
            "<script>",
            "SELECT a.id, a.job_id AS jobId, a.user_id AS userId, a.status, a.applied_at AS appliedAt,",
            "       u.username, u.real_name AS realName,",
            "       p.school, p.major, p.graduation_year AS graduationYear, p.skills",
            "FROM job_application a",
            "INNER JOIN job j ON j.id = a.job_id AND j.is_deleted = 0 AND j.poster_id = #{posterId}",
            "INNER JOIN sys_user u ON u.id = a.user_id AND u.is_deleted = 0",
            "LEFT JOIN user_profile p ON p.user_id = a.user_id AND p.is_deleted = 0",
            "WHERE a.is_deleted = 0 AND a.job_id = #{jobId}",
            "ORDER BY a.applied_at DESC",
            "</script>"
    })
    IPage<CandidateVO> selectJobCandidates(IPage<CandidateVO> page,
                                            @Param("jobId") Long jobId,
                                            @Param("posterId") Long posterId);

    /**
     * 候选人详情（HR 视角：投递记录 + 求职者资料 + 附带简历 + 职位标题）
     */
    @Select({
            "SELECT a.id AS applicationId, a.job_id AS jobId, a.status, a.hr_remark AS hrRemark,",
            "       a.cover_letter AS coverLetter, a.applied_at AS appliedAt,",
            "       a.interview_time AS interviewTime, a.interview_location AS interviewLocation, a.interview_note AS interviewNote,",
            "       j.title AS jobTitle,",
            "       u.id AS userId, u.username, u.real_name AS realName,",
            "       p.school, p.major, p.graduation_year AS graduationYear, p.skills, p.bio,",
            "       r.id AS resumeId, r.title AS resumeTitle, r.ai_score AS resumeAiScore, r.content_json AS resumeContentJson",
            "FROM job_application a",
            "INNER JOIN job j ON j.id = a.job_id AND j.is_deleted = 0",
            "INNER JOIN sys_user u ON u.id = a.user_id AND u.is_deleted = 0",
            "LEFT JOIN user_profile p ON p.user_id = a.user_id AND p.is_deleted = 0",
            "LEFT JOIN resume r ON r.id = a.resume_id AND r.is_deleted = 0",
            "WHERE a.is_deleted = 0 AND a.id = #{appId} AND j.poster_id = #{posterId}"
    })
    CandidateDetailVO selectCandidateDetail(@Param("appId") Long appId, @Param("posterId") Long posterId);

    /**
     * 候选人的徽章摘要（公开指纹前8位，与 UserBadgeVO 规则一致）
     */
    @Select({
            "SELECT t.name, t.rarity, b.earned_at AS earnedAt,",
            "       CASE WHEN b.is_locked = 1 THEN LEFT(b.lock_hash, 8) ELSE NULL END AS fingerprint",
            "FROM user_badge b",
            "INNER JOIN badge_template t ON t.id = b.badge_id AND t.is_deleted = 0",
            "WHERE b.user_id = #{userId}",
            "ORDER BY b.earned_at DESC"
    })
    List<CandidateDetailVO.BadgeSummary> selectCandidateBadges(@Param("userId") Long userId);
}