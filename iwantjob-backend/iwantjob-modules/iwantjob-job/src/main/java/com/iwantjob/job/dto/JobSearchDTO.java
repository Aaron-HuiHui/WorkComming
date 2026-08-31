package com.iwantjob.job.dto;

import com.iwantjob.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 职位搜索请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "职位搜索请求")
public class JobSearchDTO extends PageParam implements Serializable {

    @Schema(description = "关键词（命中 title/description/requirements 全文索引）", example = "Java")
    private String keyword;

    @Schema(description = "职位类型：0实习/1校招/2社招", example = "1")
    private Integer type;

    @Schema(description = "工作城市", example = "北京")
    private String city;
}
