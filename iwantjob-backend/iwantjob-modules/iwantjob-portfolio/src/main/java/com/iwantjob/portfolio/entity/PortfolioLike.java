package com.iwantjob.portfolio.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品点赞表 portfolio_like（物理删除，unique 防重复点赞）
 */
@Data
@TableName("portfolio_like")
public class PortfolioLike implements Serializable {

    @TableId
    private Long id;

    private Long portfolioId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}