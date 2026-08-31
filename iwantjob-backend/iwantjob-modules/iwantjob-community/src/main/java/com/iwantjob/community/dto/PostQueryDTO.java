package com.iwantjob.community.dto;

import com.iwantjob.common.result.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子分页查询 DTO
 * 支持按类型过滤 + FULLTEXT 关键字搜索
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQueryDTO extends PageParam implements Serializable {

    /** 关键字（触发 FULLTEXT 检索，为空则全量分页） */
    private String keyword;

    @Min(value = 0, message = "帖子类型非法")
    @Max(value = 4, message = "帖子类型非法")
    private Integer type;
}
