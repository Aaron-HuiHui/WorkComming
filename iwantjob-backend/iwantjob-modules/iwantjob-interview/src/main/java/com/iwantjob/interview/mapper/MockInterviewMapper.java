package com.iwantjob.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.interview.entity.MockInterview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模拟面试 Mapper
 */
@Mapper
public interface MockInterviewMapper extends BaseMapper<MockInterview> {
}
