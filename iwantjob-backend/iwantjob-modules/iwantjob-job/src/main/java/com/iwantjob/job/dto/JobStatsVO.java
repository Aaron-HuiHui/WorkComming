package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 岗位市场统计 VO（学生可视化）
 */
@Data
@Schema(description = "岗位市场统计总览")
public class JobStatsVO implements Serializable {

    @Schema(description = "在招职位总数")
    private Long totalJobs;

    @Schema(description = "参与发布的企业数")
    private Long totalCompanies;

    @Schema(description = "按城市分布")
    private List<NameValueVO> cityDist;

    @Schema(description = "按职位类型分布：0实习/1校招/2社招")
    private List<NameValueVO> typeDist;

    @Schema(description = "按招聘批次分布：0日常/1春招/2秋招/3实习批")
    private List<NameValueVO> batchDist;

    @Schema(description = "按薪资段分布")
    private List<NameValueVO> salaryDist;

    @Schema(description = "浏览量 TOP 职位")
    private List<NameValueVO> hotJobs;
}