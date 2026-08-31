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
 * 薪资贡献数据表 salary_report_data
 * 仅用于积分发放与去重，对外查询禁止按 user_id 反查明细
 */
@Data
@TableName("salary_report_data")
public class SalaryReportData implements Serializable {

    @TableId
    private Long id;

    /**
     * 贡献者用户ID（仅用于积分发放与去重，禁止对外反查）
     */
    private Long userId;

    private String city;

    private String position;

    /**
     * 薪资下限（元/月）
     */
    private Integer salaryMin;

    /**
     * 薪资上限（元/月）
     */
    private Integer salaryMax;

    /**
     * 公司规模档位（不存公司全称）
     */
    private String companyScale;

    private String industry;

    /**
     * JobTypeEnum: 0-实习,1-校招,2-社招
     */
    private Integer jobType;

    /**
     * EduEnum: 0-专科,1-本科,2-硕士,3-博士,4-其他
     */
    private Integer educationLevel;

    /**
     * 是否双一流：0-否,1-是
     */
    private Integer isDoubleFirstClass;

    /**
     * offer 月份，格式 yyyy-MM
     */
    private String offerMonth;

    /**
     * 是否匿名：默认1且不可改为0
     */
    private Integer isAnonymous;

    /**
     * 审核状态：SalaryVerifiedEnum 0-待审核,1-通过,2-驳回
     */
    private Integer verified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
