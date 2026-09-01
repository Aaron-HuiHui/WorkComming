package com.iwantjob.job.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位表 job
 */
@Data
@TableName("job")
public class Job implements Serializable {

    @TableId
    private Long id;

    private String title;

    private String companyName;

    /**
     * 关联企业ID（无匹配企业时为空）
     */
    private Long companyId;

    /**
     * 招聘批次 JobBatchEnum: 0-日常,1-春招,2-秋招,3-实习批
     */
    private Integer recruitmentBatch;

    /**
     * 职位类型 JobTypeEnum: 0-实习,1-校招,2-社招
     */
    private Integer jobType;

    private String description;

    private String requirements;

    private String salaryRange;

    private String location;

    /**
     * 来源：0-用户发布,1-聚合抓取
     */
    private Integer source;

    private String contactEmail;

    private LocalDateTime expiryDate;

    private Integer viewCount;

    /**
     * 发布者用户ID
     */
    private Long posterId;

    /**
     * 状态：1-正常,0-下架
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
