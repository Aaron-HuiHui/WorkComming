package com.iwantjob.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 回答请求 DTO
 */
@Data
public class AnswerCreateDTO implements Serializable {

    @NotBlank(message = "回答内容不能为空")
    private String content;
}
