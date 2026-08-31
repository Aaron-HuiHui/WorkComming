package com.iwantjob.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起技能交换请求 DTO
 */
@Data
public class SkillExchangeCreateDTO implements Serializable {

    @NotNull(message = "目标用户不能为空")
    private Long toUserId;

    @NotBlank(message = "提供技能不能为空")
    @Size(max = 50, message = "提供技能最长50字")
    private String offerSkill;

    @NotBlank(message = "期望技能不能为空")
    @Size(max = 50, message = "期望技能最长50字")
    private String wantSkill;
}
