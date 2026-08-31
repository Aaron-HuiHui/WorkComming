package com.iwantjob.helpgroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 求助列表项 VO（不含描述与反馈，仅摘要字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpRequestListVO implements Serializable {

    private Long id;
    private Long applicantId;
    private Integer reasonType;
    private String matchTags;
    private Integer status;
    private Long supporterId;
    private LocalDateTime matchedAt;
    private LocalDateTime createdAt;

    /** 当前用户在该请求中的角色：applicant-求助者 / supporter-支援者 */
    private String role;
}
