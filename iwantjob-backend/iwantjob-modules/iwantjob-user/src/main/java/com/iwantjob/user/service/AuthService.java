package com.iwantjob.user.service;

import com.iwantjob.user.dto.LoginDTO;
import com.iwantjob.user.dto.LoginVO;
import com.iwantjob.user.dto.RefreshDTO;
import com.iwantjob.user.dto.RegisterDTO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param dto 注册请求
     * @return 新用户ID
     */
    Long register(RegisterDTO dto);

    /**
     * 登录
     *
     * @param dto 登录请求
     * @return 登录响应（含token）
     */
    LoginVO login(LoginDTO dto);

    /**
     * 刷新token
     *
     * @param dto 含refreshToken
     * @return 新的登录响应
     */
    LoginVO refresh(RefreshDTO dto);

    /**
     * 登出：将 accessToken 加入 Redis 黑名单
     *
     * @param accessToken 当前访问令牌
     */
    void logout(String accessToken);
}
