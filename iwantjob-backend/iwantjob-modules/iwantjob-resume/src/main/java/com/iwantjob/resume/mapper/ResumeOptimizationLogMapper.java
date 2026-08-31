package com.iwantjob.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.resume.entity.ResumeOptimizationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历优化日志 Mapper（仅追加，无更新/删除）
 */
@Mapper
public interface ResumeOptimizationLogMapper extends BaseMapper<ResumeOptimizationLog> {
}
