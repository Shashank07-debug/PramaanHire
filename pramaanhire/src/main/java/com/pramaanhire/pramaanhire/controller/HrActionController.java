package com.pramaanhire.pramaanhire.controller;

import com.pramaanhire.pramaanhire.dto.*;
import com.pramaanhire.pramaanhire.service.ExcelExportService;
import com.pramaanhire.pramaanhire.service.HrActionService;
import com.pramaanhire.pramaanhire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Tag(name = "HR Application Management", description = "Endpoints for HR to review applications")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class HrActionController {

    private final HrActionService hrActionService;
    private final ExcelExportService excelExportService;
    private final JwtUtil jwtUtil;

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get all applications for a specific job with filtering and searching")
    public ResponseEntity<Page<HrApplicationSummaryDto>> getApplicationsForJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @Parameter(description = "Filter by status (e.g., SUBMITTED, SHORTLISTED)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by candidate name or email") 
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "aiScore", direction = Sort.Direction.DESC) Pageable pageable) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(hrActionService.getApplicationsForJob(jobId, hrId, status, search, pageable));
    }

    @GetMapping("/jobs/{jobId}/applications/export")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Export all applications for a job to Excel")
    public ResponseEntity<byte[]> exportApplications(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId) {
        
        log.info("Received request to export applications for Job ID: {}", jobId);
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        byte[] excelData = excelExportService.exportApplicationsToExcel(jobId, hrId);
        
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("applications_job_" + jobId + ".xlsx")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentLength(excelData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping("/jobs/{jobId}/shortlist-top")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Bulk Action: Shortlist Top N candidates by AI Score, Reject others")
    public ResponseEntity<?> shortlistTopCandidates(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @Valid @RequestBody BulkShortlistRequest request) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        hrActionService.shortlistTopCandidates(jobId, request.getTopN(), hrId);
        
        return ResponseEntity.ok(Map.of("message", "Bulk action completed successfully"));
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get full details of a specific application including AI evaluation")
    public ResponseEntity<HrApplicationDetailDto> getApplicationDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(hrActionService.getApplicationDetails(applicationId, hrId));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Update application status (e.g., SHORTLISTED, REJECTED)")
    public ResponseEntity<?> updateApplicationStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        hrActionService.updateApplicationStatus(applicationId, request, hrId);
        
        return ResponseEntity.ok(Map.of("message", "Application status updated successfully"));
    }

    @GetMapping("/applications/{applicationId}/actions")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get allowed status transitions for an application")
    public ResponseEntity<AllowedActionsDto> getAllowedActions(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));

        return ResponseEntity.ok(hrActionService.getAllowedActions(applicationId, hrId));
    }
}
