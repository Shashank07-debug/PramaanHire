package com.pramaanhire.pramaanhire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionEmailDto {
    private String candidateEmail;
    private String candidateName;
    private String jobTitle;
    private String strengths;
    private String weaknesses;
    private String improvementTips;
}
