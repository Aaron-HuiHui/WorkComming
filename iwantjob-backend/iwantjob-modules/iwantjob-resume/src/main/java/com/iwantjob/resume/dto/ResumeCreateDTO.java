package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建简历请求 DTO
 */
@Data
@Schema(description = "创建简历请求")
public class ResumeCreateDTO implements Serializable {

    @Schema(description = "简历标题", example = "我的后端简历")
    @NotBlank(message = "简历标题不能为空")
    @Size(max = 100, message = "简历标题长度不能超过100")
    private String title;

    @Schema(description = "简历内容 JSON 字符串（基本信息/教育/工作/项目/技能等）")
    @NotBlank(message = "简历内容不能为空")
    private String contentJson;

    @Schema(description = "是否设为默认简历：1-是,0-否", example = "0")
    private Integer isDefault;
}
