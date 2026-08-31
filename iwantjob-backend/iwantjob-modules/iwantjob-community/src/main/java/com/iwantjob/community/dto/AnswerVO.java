package com.iwantjob.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回答 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerVO implements Serializable {

    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private Integer isAccepted;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
