package com.iwantjob.helpgroup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起求助请求 DTO
 */
@Data
public class HelpRequestCreateDTO implements Serializable {

    @NotNull(message = "求助原因类型不能为空")
    @Min(value = 0, message = "求助原因类型非法")
    @Max(value = 3, message = "求助原因类型非法")
    private Integer reasonType;

    @NotBlank(message = "求助描述不能为空")
    @Size(max = 1000, message = "求助描述最长1000字")
    private String description;

    /** 匹配标签，如目标行业/城市，逗号分隔 */
    @Size(max = 200, message = "匹配标签最长200字")
    private String matchTags;
}
