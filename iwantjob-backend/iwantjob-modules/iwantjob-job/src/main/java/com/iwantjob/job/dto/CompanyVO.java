package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业信息 VO（含在招职位数）
 */
@Data
@Schema(description = "企业信息")
public class CompanyVO implements Serializable {

    @Schema(description = "企业ID")
    private Long id;

    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "规模")
    private String scale;

    @Schema(description = "总部")
    private String headquarters;

    @Schema(description = "LOGO emoji")
    private String logo;

    @Schema(description = "企业介绍")
    private String intro;

    @Schema(description = "企业文化")
    private String culture;

    @Schema(description = "福利待遇")
    private String welfare;

    @Schema(description = "官网")
    private String website;

    @Schema(description = "认领HR用户ID")
    private Long claimedBy;

    @Schema(description = "在招职位数")
    private Long jobCount;
}