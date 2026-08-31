package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我投递的职位视图对象（投递记录 + 职位摘要，联表查询结果）
 */
@Data
@Schema(description = "我投递的职位")
public class JobApplicationVO implements Serializable {

    @Schema(description = "投递记录ID")
    private Long id;

    @Schema(description = "职位ID")
    private Long jobId;

    @Schema(description = "投递人用户ID")
    private Long userId;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "求职信")
    private String coverLetter;

    @Schema(description = "投递状态：0投递成功/1初筛/2面试/3录用/4拒绝")
    private Integer status;

    @Schema(description = "HR备注")
    private String hrRemark;

    @Schema(description = "投递时间")
    private LocalDateTime appliedAt;

    // ===== 职位摘要（联表 job） =====

    @Schema(description = "职位标题")
    private String jobTitle;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位类型：0实习/1校招/2社招")
    private Integer jobType;

    @Schema(description = "工作城市")
    private String location;

    @Schema(description = "薪资范围")
    private String salaryRange;
}
