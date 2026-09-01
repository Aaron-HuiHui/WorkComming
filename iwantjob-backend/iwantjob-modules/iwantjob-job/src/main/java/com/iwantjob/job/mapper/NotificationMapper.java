package com.iwantjob.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.job.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内通知 Mapper（职位服务侧：仅 insert 写入）
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}