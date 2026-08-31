package com.iwantjob.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建模拟面试会话请求
 */
@Data
@Schema(description = "创建面试会话请求")
public class InterviewStartDTO implements Serializable {

    @Schema(description = "面试类型：0技术/1行为/2综合", example = "0")
    @NotNull(message = "面试类型不能为空")
    @Min(value = 0, message = "面试类型取值范围 0-2")
    @Max(value = 2, message = "面试类型取值范围 0-2")
    private Integer type;

    @Schema(description = "难度：1简单/2中等/3困难，默认1", example = "1")
    @Min(value = 1, message = "难度取值范围 1-3")
    @Max(value = 3, message = "难度取值范围 1-3")
    private Integer difficulty = 1;

    @Schema(description = "目标岗位", example = "Java后端开发")
    @Size(max = 100, message = "目标岗位长度不能超过100")
    private String targetJob;
}
