package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求 DTO
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO implements Serializable {

    @Schema(description = "用户名", example = "alice")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3~50")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅支持字母数字下划线")
    private String username;

    @Schema(description = "密码", example = "Abc123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6~50")
    private String password;

    @Schema(description = "邮箱", example = "alice@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Schema(description = "角色：0学生/1校友/2HR/3导师/9管理员", example = "0")
    @NotNull(message = "角色不能为空")
    private Integer role;
}
