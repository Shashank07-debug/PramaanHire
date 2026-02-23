package com.pramaanhire.pramaanhire.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkShortlistRequest {
    @Min(value = 1, message = "Top N must be at least 1")
    private int topN;
}
