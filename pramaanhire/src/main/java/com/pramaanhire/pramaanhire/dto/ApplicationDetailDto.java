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
public class ApplicationDetailDto {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String jobDescription;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private String resumeUrl;
    private List<AnswerDetailDto> answers;
    
    // AI Feedback
    private BigDecimal aiScore;
    private String aiSummary;
    private AiEvaluationDto aiEvaluation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetailDto {
        private String questionText;
        private String answerText;
    }

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
}
