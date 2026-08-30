package com.iwantjob.framework.security;

import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类：从上下文获取当前用户
 */
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser.getUserId();
    }

    public static Long requireCurrentUserId() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return id;
    }

    public static Integer getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser.getRole();
    }
}
