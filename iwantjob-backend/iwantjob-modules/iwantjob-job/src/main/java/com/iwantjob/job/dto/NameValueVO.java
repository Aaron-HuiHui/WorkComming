package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用 name-value 统计项（可视化图表数据点）
 */
@Data
@Schema(description = "统计项 {name, value}")
public class NameValueVO implements Serializable {

    @Schema(description = "名称（如城市/类型/薪资段）")
    private String name;

    @Schema(description = "数量")
    private Long value;

    public NameValueVO() {
    }

    public NameValueVO(String name, Long value) {
        this.name = name;
        this.value = value;
    }
}