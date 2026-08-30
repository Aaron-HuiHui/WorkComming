package com.iwantjob.user.service;

import com.iwantjob.user.dto.ProfileUpdateDTO;
import com.iwantjob.user.dto.UserInfoVO;
import com.iwantjob.user.entity.SysUser;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户ID查询用户实体
     */
    SysUser getById(Long userId);

    /**
     * 获取当前用户信息（含profile）
     */
    UserInfoVO getCurrentUserInfo(Long userId);

    /**
     * 更新个人资料
     */
    void updateProfile(Long userId, ProfileUpdateDTO dto);
}
