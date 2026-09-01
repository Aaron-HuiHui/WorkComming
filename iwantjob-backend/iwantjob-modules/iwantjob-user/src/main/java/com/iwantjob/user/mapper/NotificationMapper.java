package com.iwantjob.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.user.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内通知 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}