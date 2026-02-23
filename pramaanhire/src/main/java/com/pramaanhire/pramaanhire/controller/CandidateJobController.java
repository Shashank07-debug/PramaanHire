package com.pramaanhire.pramaanhire.controller;

import com.pramaanhire.pramaanhire.dto.ApplicationDetailDto;
import com.pramaanhire.pramaanhire.dto.ApplicationSummaryDto;
import com.pramaanhire.pramaanhire.dto.CandidateDashboardDto;
import com.pramaanhire.pramaanhire.dto.JobDetailDto;
import com.pramaanhire.pramaanhire.service.ApplicationService;
import com.pramaanhire.pramaanhire.service.JobService;
import com.pramaanhire.pramaanhire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/candidate/jobs")
@RequiredArgsConstructor
@Tag(name = "Candidate Job Application", description = "Endpoints for candidates to apply for jobs")
@SecurityRequirement(name = "bearerAuth")
public class CandidateJobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{jobId}/prepare-application")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get full job details including screening questions for application")
    public ResponseEntity<JobDetailDto> getJobDetailsForApplication(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobDetailsForCandidate(jobId));
    }

    @PostMapping(value = "/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit job application with resume and answers")
    public ResponseEntity<?> applyForJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @Parameter(description = "Resume PDF file", required = true) 
            @RequestParam("resume") MultipartFile resume,
            @Parameter(description = "JSON string of answers list: [{\"questionId\": 1, \"answerText\": \"My answer\"}]", 
                       schema = @Schema(type = "string", format = "json"))
            @RequestParam("answers") String answersJson) {
        
        String jwt = token.substring(7);
        Long candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        Long applicationId = applicationService.submitApplication(candidateId, jobId, answersJson, resume);
        
        return ResponseEntity.ok(Map.of("message", "Application submitted successfully", "applicationId", applicationId));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get all applications submitted by the logged-in candidate")
    public ResponseEntity<Page<ApplicationSummaryDto>> getMyApplications(
            @RequestHeader("Authorization") String token,
            @PageableDefault(sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        String jwt = token.substring(7);
        Long candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(applicationService.getMyApplications(candidateId, pageable));
    }

    @GetMapping("/my-applications/{applicationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get full details of a specific application")
    public ResponseEntity<ApplicationDetailDto> getApplicationDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        
        String jwt = token.substring(7);
        Long candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(applicationService.getApplicationDetails(applicationId, candidateId));
    }

    @PostMapping("/my-applications/{applicationId}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw a submitted application (only if status is SUBMITTED)")
    public ResponseEntity<?> withdrawApplication(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        
        String jwt = token.substring(7);
        Long candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        applicationService.withdrawApplication(applicationId, candidateId);
        
        return ResponseEntity.ok(Map.of("message", "Application withdrawn successfully"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get candidate dashboard stats and recent activity")
    public ResponseEntity<CandidateDashboardDto> getDashboard(
            @RequestHeader("Authorization") String token) {
        
        String jwt = token.substring(7);
        Long candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(applicationService.getCandidateDashboard(candidateId));
    }
}
