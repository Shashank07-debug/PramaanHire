package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRetryScheduler {

    private final ApplicationRepository applicationRepository;
    private final AiEvaluationService aiEvaluationService;
    private final PdfExtractionService pdfExtractionService;
    private final FileStorageService fileStorageService;

    // Run every 5 minutes (300,000 ms)
    @Scheduled(fixedDelay = 300000)
    public void retryFailedEvaluations() {
        log.info("Checking for pending AI evaluations...");

        List<Application> pendingApplications = applicationRepository.findByIsAiProcessedFalse();

        if (pendingApplications.isEmpty()) {
            log.info("No pending evaluations found.");
            return;
        }

        log.info("Found {} applications pending AI evaluation.", pendingApplications.size());

        for (Application app : pendingApplications) {
            try {
                log.info("Retrying AI evaluation for Application ID: {}", app.getId());
                
                // Load the stored resume file
                Resource resumeResource = fileStorageService.loadFileAsResource(app.getResumeUrl());
                
                // Extract text again
                String resumeText = pdfExtractionService.extractTextFromResource(resumeResource);
                
                // Trigger evaluation (this is async, but that's fine)
                aiEvaluationService.evaluateApplication(app.getId(), resumeText);
                
            } catch (Exception e) {
                log.error("Failed to retry evaluation for Application ID: " + app.getId(), e);
            }
        }
    }
}
