package com.iwantjob.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 投递职位请求 DTO
 */
@Data
@Schema(description = "投递职位请求")
public class JobApplyDTO implements Serializable {

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "求职信")
    @Size(max = 2000, message = "求职信长度不能超过2000")
    private String coverLetter;
}
