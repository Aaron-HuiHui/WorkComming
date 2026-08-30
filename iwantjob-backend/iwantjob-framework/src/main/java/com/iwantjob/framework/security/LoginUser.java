package com.iwantjob.framework.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户主体
 */
@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private Integer role;
}
