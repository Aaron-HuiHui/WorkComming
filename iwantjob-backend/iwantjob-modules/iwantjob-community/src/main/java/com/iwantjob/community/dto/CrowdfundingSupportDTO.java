package com.iwantjob.community.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支持众筹请求 DTO
 */
@Data
public class CrowdfundingSupportDTO implements Serializable {

    @NotNull(message = "支持金额不能为空")
    @DecimalMin(value = "0.01", message = "支持金额必须大于0")
    private BigDecimal amount;
}
