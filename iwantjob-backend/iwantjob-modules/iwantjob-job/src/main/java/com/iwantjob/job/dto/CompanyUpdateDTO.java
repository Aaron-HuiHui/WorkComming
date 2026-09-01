package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * HR 编辑企业信息 DTO（认领 + 更新）
 */
@Data
@Schema(description = "企业信息编辑请求")
public class CompanyUpdateDTO implements Serializable {

    @Schema(description = "行业")
    @Size(max = 50)
    private String industry;

    @Schema(description = "规模")
    @Size(max = 30)
    private String scale;

    @Schema(description = "总部")
    @Size(max = 50)
    private String headquarters;

    @Schema(description = "LOGO emoji")
    @Size(max = 255)
    private String logo;

    @Schema(description = "企业介绍")
    @Size(max = 2000)
    private String intro;

    @Schema(description = "企业文化")
    @Size(max = 500)
    private String culture;

    @Schema(description = "福利待遇")
    @Size(max = 500)
    private String welfare;

    @Schema(description = "官网")
    @Size(max = 255)
    private String website;
}