package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待审核薪资数据列表项 VO（管理员视角）
 * 含 3σ 异常标记
 */
@Data
@Schema(description = "待审核薪资数据")
public class PendingSalaryVO implements Serializable {

    @Schema(description = "贡献记录ID")
    private Long id;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "岗位")
    private String position;

    @Schema(description = "薪资下限（元/月）")
    private Integer salaryMin;

    @Schema(description = "薪资上限（元/月）")
    private Integer salaryMax;

    @Schema(description = "公司规模档位")
    private String companyScale;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "职位类型：0-实习,1-校招,2-社招")
    private Integer jobType;

    @Schema(description = "学历层次：0-专科,1-本科,2-硕士,3-博士,4-其他")
    private Integer educationLevel;

    @Schema(description = "是否双一流：0-否,1-是")
    private Integer isDoubleFirstClass;

    @Schema(description = "offer月份")
    private String offerMonth;

    @Schema(description = "审核状态：0-待审核,1-通过,2-驳回")
    private Integer verified;

    @Schema(description = "提交时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否为 3σ 异常数据（超出历史均值±3σ），需重点复核")
    private Boolean anomalyFlag;
}
