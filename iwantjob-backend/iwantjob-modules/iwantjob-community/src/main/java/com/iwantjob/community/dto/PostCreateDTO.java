package com.iwantjob.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发帖请求 DTO
 */
@Data
public class PostCreateDTO implements Serializable {

    @NotNull(message = "帖子类型不能为空")
    @Min(value = 0, message = "帖子类型非法")
    @Max(value = 4, message = "帖子类型非法")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 200, message = "标签最长200字")
    private String tags;
}
