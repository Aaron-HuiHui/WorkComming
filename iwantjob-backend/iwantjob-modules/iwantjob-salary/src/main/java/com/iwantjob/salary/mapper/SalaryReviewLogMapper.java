package com.iwantjob.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.salary.entity.SalaryReviewLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪资审核日志 Mapper
 */
@Mapper
public interface SalaryReviewLogMapper extends BaseMapper<SalaryReviewLog> {
}
