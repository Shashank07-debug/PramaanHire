package com.pramaanhire.pramaanhire.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pramaanhire.pramaanhire.enums.EmploymentType;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDetailDto {
    @JsonProperty("jobId")
    private Long id;
    
    private String title;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private JobStatus status;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
    private List<JobQuestionDto> questions;
    private boolean hasApplied;
    private Long applicationId; // Added field to link to the specific application
}
