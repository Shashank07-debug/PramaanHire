package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowedActionsDto {
    private Long applicationId;
    private ApplicationStatus currentStatus;
    private List<ApplicationStatus> allowedTransitions;
}
