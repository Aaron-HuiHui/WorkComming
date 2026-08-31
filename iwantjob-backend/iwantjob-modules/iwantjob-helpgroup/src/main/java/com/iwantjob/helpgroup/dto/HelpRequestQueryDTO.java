package com.iwantjob.helpgroup.dto;

import com.iwantjob.common.result.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帮帮团求助分页查询 DTO
 * 支持按原因类型过滤 + 标签筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HelpRequestQueryDTO extends PageParam implements Serializable {

    @Min(value = 0, message = "求助原因类型非法")
    @Max(value = 3, message = "求助原因类型非法")
    private Integer reasonType;

    /** 标签关键字（模糊匹配 match_tags） */
    private String tag;
}
