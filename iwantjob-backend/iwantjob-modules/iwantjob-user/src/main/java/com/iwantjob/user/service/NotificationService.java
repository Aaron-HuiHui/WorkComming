package com.iwantjob.user.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.user.dto.NotificationVO;

/**
 * 站内通知服务接口
 */
public interface NotificationService {

    /** 我的通知分页（按时间倒序） */
    PageResult<NotificationVO> myNotifications(Long userId, long page, long size);

    /** 未读数量 */
    Long unreadCount(Long userId);

    /** 标记单条已读（仅本人） */
    void markRead(Long userId, Long id);

    /** 全部标记已读 */
    void markAllRead(Long userId);
}