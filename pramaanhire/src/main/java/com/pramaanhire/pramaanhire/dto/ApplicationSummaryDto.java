package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryDto {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String location;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
}
