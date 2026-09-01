package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我发布的职位视图对象（职位 + 投递人数统计，HR 工作台用）
 */
@Data
@Schema(description = "我发布的职位（含投递统计）")
public class HrJobVO implements Serializable {

    @Schema(description = "职位ID")
    private Long id;

    @Schema(description = "职位标题")
    private String title;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位类型：0实习/1校招/2社招")
    private Integer jobType;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "工作城市")
    private String location;

    @Schema(description = "状态：1正常/0下架")
    private Integer status;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;

    @Schema(description = "该职位收到的投递数")
    private Long applicationCount;
}