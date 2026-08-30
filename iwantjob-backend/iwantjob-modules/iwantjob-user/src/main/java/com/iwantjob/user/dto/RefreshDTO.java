package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷新token请求 DTO
 */
@Data
@Schema(description = "刷新token请求")
public class RefreshDTO implements Serializable {

    @Schema(description = "refreshToken", example = "eyJhbGciOi...")
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
