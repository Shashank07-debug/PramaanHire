package com.pramaanhire.pramaanhire.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramaanhire.pramaanhire.dto.ApplicationAnswerDto;
import com.pramaanhire.pramaanhire.dto.ApplicationDetailDto;
import com.pramaanhire.pramaanhire.dto.ApplicationSummaryDto;
import com.pramaanhire.pramaanhire.dto.CandidateDashboardDto;
import com.pramaanhire.pramaanhire.entity.*;
import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import com.pramaanhire.pramaanhire.enums.Role;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import com.pramaanhire.pramaanhire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final AiEvaluationService aiEvaluationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional
    public Long submitApplication(Long candidateId, Long jobId, String answersJson, MultipartFile resume) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new AccessDeniedException("Only candidates can apply for jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Validate Job Status
        if (job.getStatus() != JobStatus.OPEN || !job.isActive()) {
            throw new RuntimeException("This job is no longer accepting applications");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("The application deadline for this job has passed");
        }

        // Check for duplicate application
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new RuntimeException("You have already applied for this job");
        }

        // Validate Resume
        if (resume.isEmpty() || !resume.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Only PDF resumes are allowed");
        }

        // Store Resume
        String storedFileName = fileStorageService.storeFile(resume);

        // Extract text from PDF
        String resumeText = pdfExtractionService.extractTextFromPdf(resume);

        // Parse Answers
        List<ApplicationAnswerDto> answerDtos;
        try {
            answerDtos = objectMapper.readValue(answersJson, new TypeReference<List<ApplicationAnswerDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid answers format");
        }

        // Validate Answers against Job Questions
        validateAnswers(job, answerDtos);

        // Create Application
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resumeUrl(storedFileName)
                .status(ApplicationStatus.SUBMITTED)
                .isAiProcessed(false) // Initially false
                .answers(new ArrayList<>())
                .build();

        // Save Answers
        for (ApplicationAnswerDto ansDto : answerDtos) {
            // Skip empty answers (unless mandatory, which is caught by validateAnswers)
            if (ansDto.getAnswerText() == null || ansDto.getAnswerText().trim().isEmpty()) {
                continue;
            }

            JobQuestion question = job.getQuestions().stream()
                    .filter(q -> q.getId().equals(ansDto.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid question ID: " + ansDto.getQuestionId()));

            ApplicationAnswer answer = ApplicationAnswer.builder()
                    .application(application)
                    .question(question)
                    .answerText(ansDto.getAnswerText())
                    .build();
            
            application.getAnswers().add(answer);
        }

        Application savedApp = applicationRepository.save(application);

        // Trigger Async AI Evaluation AFTER transaction commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                aiEvaluationService.evaluateApplication(savedApp.getId(), resumeText);
                // Send Submission Confirmation Email
                emailService.sendSubmissionEmail(candidate.getEmail(), candidate.getFullName(), job.getTitle());
            }
        });

        return savedApp.getId();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationSummaryDto> getMyApplications(Long candidateId, Pageable pageable) {
        return applicationRepository.findByCandidateId(candidateId, pageable)
                .map(app -> ApplicationSummaryDto.builder()
                        .applicationId(app.getId())
                        .jobId(app.getJob().getId())
                        .jobTitle(app.getJob().getTitle())
                        .location(app.getJob().getLocation())
                        .status(app.getStatus())
                        .submittedAt(app.getSubmittedAt())
                        .build());
    }

    @Transactional(readOnly = true)
    public ApplicationDetailDto getApplicationDetails(Long applicationId, Long candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new AccessDeniedException("You are not authorized to view this application");
        }

        ApplicationDetailDto.AiEvaluationDto aiEvalDto = null;
        if (application.getAiEvaluation() != null) {
            aiEvalDto = ApplicationDetailDto.AiEvaluationDto.builder()
                    .strengths(application.getAiEvaluation().getStrengths())
                    .weaknesses(application.getAiEvaluation().getWeaknesses())
                    .improvementTips(application.getAiEvaluation().getImprovementTips())
                    .confidenceScore(application.getAiEvaluation().getConfidenceScore())
                    .modelUsed(application.getAiEvaluation().getModelUsed())
                    .build();
        }

        return ApplicationDetailDto.builder()
                .applicationId(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .jobDescription(application.getJob().getDescription())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .resumeUrl(fileBaseUrl + application.getResumeUrl())
                .aiScore(application.getAiScore())
                .aiSummary(application.getAiSummary())
                .aiEvaluation(aiEvalDto)
                .answers(application.getAnswers().stream()
                        .map(ans -> ApplicationDetailDto.AnswerDetailDto.builder()
                                .questionText(ans.getQuestion().getQuestionText())
                                .answerText(ans.getAnswerText())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void withdrawApplication(Long applicationId, Long candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new AccessDeniedException("You are not authorized to modify this application");
        }

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("Application cannot be withdrawn as it is already under review or processed.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public CandidateDashboardDto getCandidateDashboard(Long candidateId) {
        // Fetch all applications for stats
        List<Application> allApps = applicationRepository.findByCandidateId(candidateId, Pageable.unpaged()).getContent();
        
        // Status Breakdown
        Map<String, Long> statusBreakdown = allApps.stream()
                .collect(Collectors.groupingBy(app -> app.getStatus().name(), Collectors.counting()));

        // Recent Applications (Top 5)
        List<ApplicationSummaryDto> recentApps = allApps.stream()
                .sorted(Comparator.comparing(Application::getSubmittedAt).reversed())
                .limit(5)
                .map(app -> ApplicationSummaryDto.builder()
                        .applicationId(app.getId())
                        .jobId(app.getJob().getId())
                        .jobTitle(app.getJob().getTitle())
                        .location(app.getJob().getLocation())
                        .status(app.getStatus())
                        .submittedAt(app.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());

        // AI Stats
        DoubleSummaryStatistics aiStats = allApps.stream()
                .map(Application::getAiScore)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();
        
        double latestScore = allApps.stream()
                .filter(a -> a.getAiScore() != null)
                .sorted(Comparator.comparing(Application::getSubmittedAt).reversed())
                .map(a -> a.getAiScore().doubleValue())
                .findFirst()
                .orElse(0.0);

        // Application Trend (Last 30 Days)
        Map<String, Long> trend = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String key = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            long count = allApps.stream()
                    .filter(a -> a.getSubmittedAt().toLocalDate().equals(date))
                    .count();
            trend.put(key, count);
        }

        return CandidateDashboardDto.builder()
                .totalApplications(allApps.size())
                .statusBreakdown(statusBreakdown)
                .recentApplications(recentApps)
                .averageAiScore(aiStats.getCount() > 0 ? Math.round(aiStats.getAverage() * 10.0) / 10.0 : 0.0)
                .highestAiScore(aiStats.getCount() > 0 ? aiStats.getMax() : 0.0)
                .latestAiScore(latestScore)
                .applicationsTrend(trend)
                .build();
    }

    private void validateAnswers(Job job, List<ApplicationAnswerDto> answerDtos) {
        Map<Long, ApplicationAnswerDto> answerMap = answerDtos.stream()
                .collect(Collectors.toMap(ApplicationAnswerDto::getQuestionId, Function.identity()));

        for (JobQuestion question : job.getQuestions()) {
            if (question.isMandatory()) {
                if (!answerMap.containsKey(question.getId()) || 
                    answerMap.get(question.getId()).getAnswerText() == null || 
                    answerMap.get(question.getId()).getAnswerText().trim().isEmpty()) {
                    throw new RuntimeException("Missing answer for mandatory question: " + question.getQuestionText());
                }
            }
            
            if (answerMap.containsKey(question.getId())) {
                String answerText = answerMap.get(question.getId()).getAnswerText();
                if (question.getMaxLength() != null && answerText.length() > question.getMaxLength()) {
                    throw new RuntimeException("Answer exceeds max length for question: " + question.getQuestionText());
                }
            }
        }
    }
}
