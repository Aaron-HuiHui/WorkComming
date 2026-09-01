package com.iwantjob.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知 VO
 */
@Data
@Schema(description = "站内通知")
public class NotificationVO implements Serializable {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "类型：0系统/1投递状态/2面试邀请")
    private Integer type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "关联业务ID")
    private Long relatedId;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "时间")
    private LocalDateTime createdAt;
}