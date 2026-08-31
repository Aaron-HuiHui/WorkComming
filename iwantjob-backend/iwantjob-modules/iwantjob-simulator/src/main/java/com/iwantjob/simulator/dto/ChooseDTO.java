package com.iwantjob.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交选择请求 DTO
 */
@Data
@Schema(description = "提交模拟选择")
public class ChooseDTO implements Serializable {

    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @Schema(description = "选项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "选项ID不能为空")
    private Long optionId;
}
