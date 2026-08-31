package com.iwantjob.community.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子实体
 * 对应表 post，含 FULLTEXT 索引 ft_post_search(title, content)
 */
@Data
@TableName("post")
public class Post implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID */
    private Long authorId;

    /** PostTypeEnum: 0-问答,1-面经,2-技能交换,3-生活互助,4-其他 */
    private Integer type;

    private String title;

    private String content;

    /** 标签，逗号分隔 */
    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    /** 是否置顶：0-否,1-是 */
    private Integer isPinned;

    /** 是否已解决：0-否,1-是 */
    private Integer isSolved;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
