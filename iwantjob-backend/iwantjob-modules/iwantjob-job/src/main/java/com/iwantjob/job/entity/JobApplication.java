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
 * 职位投递表 job_application
 */
@Data
@TableName("job_application")
public class JobApplication implements Serializable {

    @TableId
    private Long id;

    private Long jobId;

    private Long userId;

    private Long resumeId;

    private String coverLetter;

    /**
     * 投递状态 ApplicationStatusEnum: 0-投递成功,1-初筛,2-面试,3-录用,4-拒绝
     */
    private Integer status;

    private String hrRemark;

    /**
     * 投递时间：依赖 DB DEFAULT CURRENT_TIMESTAMP，插入时不传
     */
    private LocalDateTime appliedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
