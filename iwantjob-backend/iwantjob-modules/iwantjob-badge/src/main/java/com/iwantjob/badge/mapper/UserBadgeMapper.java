package com.iwantjob.badge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.badge.entity.UserBadge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户徽章 Mapper
 * <p>
 * 注意：user_badge 表由 DB 触发器禁止 UPDATE 与 DELETE，
 * 本 Mapper 仅用于 SELECT 与 INSERT，不得调用 updateById、delete 等系列方法。
 */
@Mapper
public interface UserBadgeMapper extends BaseMapper<UserBadge> {
}
