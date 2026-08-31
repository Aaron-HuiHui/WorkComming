package com.iwantjob.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能交换 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillExchangeVO implements Serializable {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String offerSkill;
    private String wantSkill;
    private Integer status;
    private LocalDateTime createdAt;
}
