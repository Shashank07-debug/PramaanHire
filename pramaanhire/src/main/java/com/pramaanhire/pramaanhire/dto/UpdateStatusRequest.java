package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
    
    private String hrNotes;
}
