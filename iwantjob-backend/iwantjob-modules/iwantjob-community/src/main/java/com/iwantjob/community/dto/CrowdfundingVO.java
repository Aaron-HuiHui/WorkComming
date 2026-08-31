package com.iwantjob.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 众筹项目 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdfundingVO implements Serializable {

    private Long id;
    private Long initiatorId;
    private String title;
    private String description;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private Integer status;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
