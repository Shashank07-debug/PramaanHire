package com.pramaanhire.pramaanhire.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramaanhire.pramaanhire.entity.AiEvaluation;
import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.entity.ApplicationAnswer;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.repository.AiEvaluationRepository;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEvaluationService {

    private final ChatClient.Builder chatClientBuilder;
    private final AiEvaluationRepository aiEvaluationRepository;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void evaluateApplication(Long applicationId, String resumeText) {
        log.info("Starting AI evaluation for application ID: {}", applicationId);
        
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = application.getJob();
        List<ApplicationAnswer> answers = application.getAnswers();

        String promptText = buildPrompt(job, answers, resumeText);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            
            String response = chatClient.prompt()
                .user(promptText)
                .call()
                .content();

            log.info("AI Response received: {}", response);
            
            saveEvaluation(application, response);
            
        } catch (Exception e) {
            log.error("Error during AI evaluation", e);
            // We don't throw here to avoid rolling back the main transaction if this is async
            // But we should probably mark the application as failed processing or retry
        }
    }

    private String buildPrompt(Job job, List<ApplicationAnswer> answers, String resumeText) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert HR AI assistant. Evaluate the following candidate application for the job role: ")
          .append(job.getTitle()).append("\n\n");
        
        sb.append("Job Description:\n").append(job.getDescription()).append("\n\n");
        
        sb.append("Candidate Resume Text:\n").append(resumeText).append("\n\n");
        
        sb.append("Screening Questions and Answers:\n");
        for (ApplicationAnswer ans : answers) {
            sb.append("Q: ").append(ans.getQuestion().getQuestionText()).append("\n");
            sb.append("A: ").append(ans.getAnswerText()).append("\n");
        }
        
        sb.append("\nEvaluate the candidate based on the resume and answers against the job description.\n");
        sb.append("Provide the output in strict JSON format with the following fields:\n");
        sb.append("- score (number 0-100)\n");
        sb.append("- summary (string, brief overview)\n");
        sb.append("- strengths (string, comma separated)\n");
        sb.append("- weaknesses (string, comma separated)\n");
        sb.append("- improvementTips (string, actionable advice)\n");
        sb.append("- confidenceScore (number 0-100, how confident are you in this evaluation)\n");
        sb.append("Do not include markdown formatting like ```json, just the raw JSON.");
        
        return sb.toString();
    }

    private void saveEvaluation(Application application, String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            BigDecimal score = BigDecimal.valueOf(root.path("score").asDouble());
            String summary = root.path("summary").asText();
            String strengths = root.path("strengths").asText();
            String weaknesses = root.path("weaknesses").asText();
            String improvementTips = root.path("improvementTips").asText();
            BigDecimal confidenceScore = BigDecimal.valueOf(root.path("confidenceScore").asDouble());

            // Update Application
            application.setAiScore(score);
            application.setAiSummary(summary);
            application.setAiProcessed(true);
            applicationRepository.save(application);

            // Save Detailed Evaluation
            AiEvaluation evaluation = AiEvaluation.builder()
                    .application(application)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .improvementTips(improvementTips)
                    .confidenceScore(confidenceScore)
                    .modelUsed("gpt-4o-mini")
                    .build();
            
            aiEvaluationRepository.save(evaluation);
            
            log.info("AI evaluation saved successfully for application ID: {}", application.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response JSON", e);
        }
    }
}
