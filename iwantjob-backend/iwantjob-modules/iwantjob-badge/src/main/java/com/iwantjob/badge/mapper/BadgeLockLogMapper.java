package com.iwantjob.badge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.badge.entity.BadgeLockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 徽章锁定审计日志 Mapper（仅 INSERT 与 SELECT）
 */
@Mapper
public interface BadgeLockLogMapper extends BaseMapper<BadgeLockLog> {
}
