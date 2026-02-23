package com.pramaanhire.pramaanhire.dto;

import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrApplicationDetailDto {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    
    // Candidate Info
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    
    // Application Data
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private String hrNotes;
    
    // AI Evaluation
    private BigDecimal aiScore;
    private String aiSummary;
    private AiEvaluationDto aiEvaluation;
    
    // Q&A
    private List<AnswerDetailDto> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiEvaluationDto {
        private String strengths;
        private String weaknesses;
        private String improvementTips;
        private BigDecimal confidenceScore;
        private String modelUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetailDto {
        private String questionText;
        private String answerText;
    }
}
