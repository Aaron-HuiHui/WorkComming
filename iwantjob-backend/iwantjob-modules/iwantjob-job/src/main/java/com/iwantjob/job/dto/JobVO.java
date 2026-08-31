package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位视图对象（列表/详情通用）
 */
@Data
@Schema(description = "职位信息")
public class JobVO implements Serializable {

    @Schema(description = "职位ID")
    private Long id;

    @Schema(description = "职位标题")
    private String title;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位类型：0实习/1校招/2社招")
    private Integer jobType;

    @Schema(description = "职位描述")
    private String description;

    @Schema(description = "任职要求")
    private String requirements;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "工作城市")
    private String location;

    @Schema(description = "来源：0用户发布/1聚合抓取")
    private Integer source;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "有效期")
    private LocalDateTime expiryDate;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "发布者用户ID")
    private Long posterId;

    @Schema(description = "状态：1正常/0下架")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
