package com.pramaanhire.pramaanhire.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobQuestionDto {
    @JsonProperty("questionId")
    private Long id;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Mandatory flag is required")
    private Boolean isMandatory;

    private Integer maxLength;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;
}
