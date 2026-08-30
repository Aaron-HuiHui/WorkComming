package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {

    @Schema(description = "用户名", example = "alice")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "Abc123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
