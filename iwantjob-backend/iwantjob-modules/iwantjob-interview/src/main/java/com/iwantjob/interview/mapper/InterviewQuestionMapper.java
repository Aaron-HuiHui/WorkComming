package com.iwantjob.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.interview.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 面试题目 Mapper
 */
@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestion> {

    /**
     * 按面试会话查询全部题目（按题目顺序升序）
     */
    @Select("SELECT * FROM interview_question WHERE mock_id = #{mockId} ORDER BY sort_order ASC")
    List<InterviewQuestion> selectByMockId(@Param("mockId") Long mockId);
}
