package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrApplicationSummaryDto {
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime submittedAt;
    private ApplicationStatus status;
    private BigDecimal aiScore;
    private String aiSummary;
}
