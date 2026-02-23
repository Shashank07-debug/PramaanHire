package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.EmploymentType;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class JobRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String location;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Status is required")
    private JobStatus status;

    @Future(message = "Application deadline must be in the future")
    private LocalDateTime applicationDeadline;

    @Valid
    private List<JobQuestionDto> questions;
}
