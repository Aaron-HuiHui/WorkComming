package com.iwantjob.helpgroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 求助详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpRequestVO implements Serializable {

    private Long id;
    private Long applicantId;
    private Integer reasonType;
    private String description;
    private String matchTags;
    private Integer status;
    private Long supporterId;
    private LocalDateTime matchedAt;
    private LocalDateTime resolvedAt;
    private String feedback;
    private LocalDateTime createdAt;

    /** 当前用户在该请求中的角色：applicant-求助者 / supporter-支援者 */
    private String role;
}
