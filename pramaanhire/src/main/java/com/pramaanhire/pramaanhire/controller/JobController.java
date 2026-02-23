package com.pramaanhire.pramaanhire.controller;

import com.pramaanhire.pramaanhire.dto.JobDetailDto;
import com.pramaanhire.pramaanhire.dto.JobRequest;
import com.pramaanhire.pramaanhire.dto.JobResponse;
import com.pramaanhire.pramaanhire.dto.JobSummaryDto;
import com.pramaanhire.pramaanhire.service.JobService;
import com.pramaanhire.pramaanhire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr/jobs")
@RequiredArgsConstructor
@Tag(name = "HR Job Management", description = "Endpoints for HR to manage jobs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Create a new job with screening questions")
    public ResponseEntity<JobResponse> createJob(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody JobRequest request) {
        
        String jwt = token.substring(7);
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(jobService.createJob(request, userId));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Update an existing job (Restricted if applications exist)")
    public ResponseEntity<JobResponse> updateJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request) {
        
        String jwt = token.substring(7);
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(jobService.updateJob(jobId, request, userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get all jobs created by the logged-in HR with pagination")
    public ResponseEntity<Page<JobSummaryDto>> getMyJobs(
            @RequestHeader("Authorization") String token,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        String jwt = token.substring(7);
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(jobService.getJobsByHr(userId, pageable));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get full details of a specific job (HR only)")
    public ResponseEntity<JobDetailDto> getJobDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId) {
        
        String jwt = token.substring(7);
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(jobService.getJobDetails(jobId, userId));
    }
}
