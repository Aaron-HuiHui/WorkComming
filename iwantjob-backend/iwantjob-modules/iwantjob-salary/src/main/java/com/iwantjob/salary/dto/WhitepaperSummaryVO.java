package com.iwantjob.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 白皮书简版 VO（公开摘要）
 * 不含完整 report_json，仅含概览信息
 */
@Data
@Schema(description = "薪资白皮书（简版摘要）")
public class WhitepaperSummaryVO implements Serializable {

    @Schema(description = "白皮书ID")
    private Long id;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "总样本量")
    private Integer totalSamples;

    @Schema(description = "覆盖城市数")
    private Integer cityCount;

    @Schema(description = "覆盖岗位数")
    private Integer positionCount;

    @Schema(description = "整体薪资中位数（P50）")
    private Integer overallMedian;

    @Schema(description = "是否需要贡献记录解锁高级章节")
    private Boolean advancedLocked;
}
