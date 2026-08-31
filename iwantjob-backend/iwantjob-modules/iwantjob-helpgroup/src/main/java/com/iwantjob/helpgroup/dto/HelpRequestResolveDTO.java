package com.iwantjob.helpgroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 完成支援请求 DTO（求助者提交反馈）
 */
@Data
public class HelpRequestResolveDTO implements Serializable {

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "反馈内容最长1000字")
    private String feedback;
}
