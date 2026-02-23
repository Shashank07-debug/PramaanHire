package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.EmploymentType;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryDto {
    private Long id;
    private String title;
    private String location;
    private EmploymentType employmentType;
    private JobStatus status;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
    private int questionCount;
    private long applicationCount;
    private boolean hasApplied; // New field for candidates
}
