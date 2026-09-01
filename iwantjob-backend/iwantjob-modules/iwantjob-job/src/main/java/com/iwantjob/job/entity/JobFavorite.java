package com.iwantjob.job.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位收藏表 job_favorite（物理删除，unique 防重复收藏）
 */
@Data
@TableName("job_favorite")
public class JobFavorite implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private Long jobId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}