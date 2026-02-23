package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.dto.AllowedActionsDto;
import com.pramaanhire.pramaanhire.dto.HrApplicationDetailDto;
import com.pramaanhire.pramaanhire.dto.HrApplicationSummaryDto;
import com.pramaanhire.pramaanhire.dto.RejectionEmailDto;
import com.pramaanhire.pramaanhire.dto.UpdateStatusRequest;
import com.pramaanhire.pramaanhire.entity.AiEvaluation;
import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrActionService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional(readOnly = true)
    public Page<HrApplicationSummaryDto> getApplicationsForJob(Long jobId, Long hrId, String status, String search, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to view applications for this job");
        }

        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by Job ID
            predicates.add(cb.equal(root.get("job").get("id"), jobId));

            // Filter by Status
            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"), ApplicationStatus.valueOf(status)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }

            // Search by Name or Email
            if (search != null && !search.isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("candidate").get("fullName")), likePattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("candidate").get("email")), likePattern);
                predicates.add(cb.or(nameMatch, emailMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return applicationRepository.findAll(spec, pageable)
                .map(app -> HrApplicationSummaryDto.builder()
                        .applicationId(app.getId())
                        .candidateName(app.getCandidate().getFullName())
                        .candidateEmail(app.getCandidate().getEmail())
                        .submittedAt(app.getSubmittedAt())
                        .status(app.getStatus())
                        .aiScore(app.getAiScore())
                        .aiSummary(app.getAiSummary())
                        .build());
    }

    @Transactional(readOnly = true)
    public HrApplicationDetailDto getApplicationDetails(Long applicationId, Long hrId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Security Check: HR must own the job
        if (!application.getJob().getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to view this application");
        }

        HrApplicationDetailDto.AiEvaluationDto aiEvalDto = null;
        if (application.getAiEvaluation() != null) {
            aiEvalDto = HrApplicationDetailDto.AiEvaluationDto.builder()
                    .strengths(application.getAiEvaluation().getStrengths())
                    .weaknesses(application.getAiEvaluation().getWeaknesses())
                    .improvementTips(application.getAiEvaluation().getImprovementTips())
                    .confidenceScore(application.getAiEvaluation().getConfidenceScore())
                    .modelUsed(application.getAiEvaluation().getModelUsed())
                    .build();
        }

        return HrApplicationDetailDto.builder()
                .applicationId(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .resumeUrl(fileBaseUrl + application.getResumeUrl())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .hrNotes(application.getHrNotes())
                .aiScore(application.getAiScore())
                .aiSummary(application.getAiSummary())
                .aiEvaluation(aiEvalDto)
                .answers(application.getAnswers().stream()
                        .map(ans -> HrApplicationDetailDto.AnswerDetailDto.builder()
                                .questionText(ans.getQuestion().getQuestionText())
                                .answerText(ans.getAnswerText())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void updateApplicationStatus(Long applicationId, UpdateStatusRequest request, Long hrId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Security Check
        if (!application.getJob().getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to update this application");
        }

        ApplicationStatus currentStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus();

        // Idempotency Check
        if (currentStatus == newStatus) {
            if (request.getHrNotes() != null && !request.getHrNotes().trim().isEmpty()) {
                application.setHrNotes(request.getHrNotes());
                applicationRepository.save(application);
            }
            return;
        }

        // Validate Transitions
        validateStatusTransition(currentStatus, newStatus);

        application.setStatus(newStatus);
        
        if (request.getHrNotes() != null && !request.getHrNotes().trim().isEmpty()) {
            application.setHrNotes(request.getHrNotes());
        }

        applicationRepository.save(application);

        // Trigger Emails based on Status Change
        triggerStatusEmail(application, newStatus);
    }

    @Transactional
    public void shortlistTopCandidates(Long jobId, int topN, Long hrId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to manage this job");
        }

        // Fetch all applications that are eligible (SUBMITTED or UNDER_REVIEW)
        List<Application> eligibleApps = applicationRepository.findByJobId(jobId, Pageable.unpaged()).getContent().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SUBMITTED || app.getStatus() == ApplicationStatus.UNDER_REVIEW)
                .sorted(Comparator.comparing(Application::getAiScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        if (eligibleApps.isEmpty()) {
            throw new RuntimeException("No eligible applications found to process.");
        }

        // Validation: Ensure we have enough candidates to shortlist
        if (eligibleApps.size() < topN) {
            throw new RuntimeException("Cannot shortlist Top " + topN + ". Only " + eligibleApps.size() + " eligible candidates available.");
        }

        int processedCount = 0;
        for (Application app : eligibleApps) {
            if (processedCount < topN) {
                // Top N -> Move to UNDER_REVIEW (Consideration)
                if (app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
                    app.setStatus(ApplicationStatus.UNDER_REVIEW);
                    app.setHrNotes("Auto-selected for review based on Top " + topN + " AI Score");
                    applicationRepository.save(app);
                }
            } else {
                // Others -> REJECTED
                app.setStatus(ApplicationStatus.REJECTED);
                app.setHrNotes("Auto-rejected: Did not make Top " + topN + " cut");
                applicationRepository.save(app);
                triggerStatusEmail(app, ApplicationStatus.REJECTED);
            }
            processedCount++;
        }
        
        log.info("Bulk action complete: Top {} moved to UNDER_REVIEW, {} rejected.", Math.min(topN, eligibleApps.size()), Math.max(0, eligibleApps.size() - topN));
    }

    private void triggerStatusEmail(Application application, ApplicationStatus newStatus) {
        String candidateEmail = application.getCandidate().getEmail();
        String candidateName = application.getCandidate().getFullName();
        String jobTitle = application.getJob().getTitle();

        if (newStatus == ApplicationStatus.SHORTLISTED) {
            emailService.sendShortlistedEmail(candidateEmail, candidateName, jobTitle);
        } else if (newStatus == ApplicationStatus.HIRED) {
            emailService.sendHiredEmail(candidateEmail, candidateName, jobTitle);
        } else if (newStatus == ApplicationStatus.REJECTED) {
            AiEvaluation eval = application.getAiEvaluation();
            RejectionEmailDto emailDto = RejectionEmailDto.builder()
                    .candidateEmail(candidateEmail)
                    .candidateName(candidateName)
                    .jobTitle(jobTitle)
                    .strengths(eval != null ? eval.getStrengths() : null)
                    .weaknesses(eval != null ? eval.getWeaknesses() : null)
                    .improvementTips(eval != null ? eval.getImprovementTips() : null)
                    .build();
            
            emailService.sendRejectionEmail(emailDto);
        }
    }

    @Transactional(readOnly = true)
    public AllowedActionsDto getAllowedActions(Long applicationId, Long hrId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJob().getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to view this application");
        }

        List<ApplicationStatus> allowed = new ArrayList<>();
        ApplicationStatus current = application.getStatus();

        switch (current) {
            case SUBMITTED:
                allowed.add(ApplicationStatus.UNDER_REVIEW);
                allowed.add(ApplicationStatus.REJECTED);
                break;
            case UNDER_REVIEW:
                allowed.add(ApplicationStatus.SHORTLISTED);
                allowed.add(ApplicationStatus.REJECTED);
                break;
            case SHORTLISTED:
                allowed.add(ApplicationStatus.HIRED);
                allowed.add(ApplicationStatus.REJECTED);
                break;
            default:
                // Terminal states have no next actions
                break;
        }

        return AllowedActionsDto.builder()
                .applicationId(application.getId())
                .currentStatus(current)
                .allowedTransitions(allowed)
                .build();
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) return; // No change is valid

        boolean isValid = false;

        switch (current) {
            case SUBMITTED:
                if (next == ApplicationStatus.UNDER_REVIEW || next == ApplicationStatus.REJECTED) isValid = true;
                break;
            case UNDER_REVIEW:
                if (next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.REJECTED) isValid = true;
                break;
            case SHORTLISTED:
                if (next == ApplicationStatus.HIRED || next == ApplicationStatus.REJECTED) isValid = true;
                break;
            case REJECTED:
            case HIRED:
            case WITHDRAWN:
                // Terminal states
                isValid = false;
                break;
            default:
                isValid = false;
        }

        if (!isValid) {
            throw new RuntimeException("Invalid status transition from " + current + " to " + next);
        }
    }
}
