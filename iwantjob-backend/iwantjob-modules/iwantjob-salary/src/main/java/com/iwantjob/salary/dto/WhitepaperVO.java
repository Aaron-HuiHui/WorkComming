package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 白皮书完整 VO（含完整 report_json）
 * 仅贡献者或管理员可见完整内容
 */
@Data
@Schema(description = "薪资白皮书（完整版）")
public class WhitepaperVO implements Serializable {

    @Schema(description = "白皮书ID")
    private Long id;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "报告JSON（含 P25/P50/P75/P99、样本量、分组等），完整版才返回")
    private String reportJson;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "访问级别：0-公开,1-贡献者专属")
    private Integer accessLevel;

    @Schema(description = "当前用户是否有权查看高级章节")
    private Boolean advancedUnlocked;
}
