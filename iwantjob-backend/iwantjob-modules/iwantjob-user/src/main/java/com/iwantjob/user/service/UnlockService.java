package com.iwantjob.user.service;

import com.iwantjob.user.dto.UnlockRecordVO;

/**
 * 权益解锁服务接口
 */
public interface UnlockService {

    /**
     * 积分解锁导师咨询
     *
     * @param userId 用户ID
     * @return 解锁记录
     */
    UnlockRecordVO unlockMentor(Long userId);
}
