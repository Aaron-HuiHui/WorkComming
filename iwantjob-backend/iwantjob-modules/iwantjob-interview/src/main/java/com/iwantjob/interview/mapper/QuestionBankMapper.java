package com.iwantjob.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.interview.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题库 Mapper
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 按分类随机抽取指定数量的题目（ORDER BY RAND() 在题库规模下性能可接受）
     */
    @Select("SELECT * FROM question_bank WHERE category = #{category} AND is_deleted = 0 ORDER BY RAND() LIMIT #{limit}")
    List<QuestionBank> selectRandomByCategory(@Param("category") Integer category, @Param("limit") int limit);
}
