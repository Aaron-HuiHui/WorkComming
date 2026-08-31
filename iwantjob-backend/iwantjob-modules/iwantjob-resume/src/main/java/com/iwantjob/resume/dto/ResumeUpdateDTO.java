package com.iwantjob.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新简历请求 DTO（title/contentJson 至少传一个）
 */
@Data
@Schema(description = "更新简历请求")
public class ResumeUpdateDTO implements Serializable {

    @Schema(description = "简历标题")
    @Size(max = 100, message = "简历标题长度不能超过100")
    private String title;

    @Schema(description = "简历内容 JSON 字符串")
    private String contentJson;

    @Schema(description = "是否设为默认简历：1-是,0-否", example = "0")
    private Integer isDefault;
}
