package com.iwantjob.salary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 薪资白皮书表 salary_whitepaper
 */
@Data
@TableName("salary_whitepaper")
public class SalaryWhitepaper implements Serializable {

    @TableId
    private Long id;

    /**
     * 版本号，如 2026-08
     */
    private String version;

    private String title;

    /**
     * 报告 JSON（含 P25/P50/P75/P99、样本量、分组等）
     */
    private String reportJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime generatedAt;

    /**
     * 访问级别：SalaryAccessLevelEnum 0-公开,1-贡献者专属
     */
    private Integer accessLevel;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
