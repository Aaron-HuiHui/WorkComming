package com.iwantjob.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailVO implements Serializable {

    private Long id;
    private Long authorId;
    private Integer type;
    private String title;
    private String content;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isPinned;
    private Integer isSolved;
    private LocalDateTime createdAt;
}
