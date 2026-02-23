package com.pramaanhire.pramaanhire.controller;

import com.pramaanhire.pramaanhire.dto.JobDetailDto;
import com.pramaanhire.pramaanhire.dto.JobSummaryDto;
import com.pramaanhire.pramaanhire.service.PublicJobService;
import com.pramaanhire.pramaanhire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Public Job Discovery", description = "Endpoints for candidates to browse jobs (No Auth Required)")
public class PublicJobController {

    private final PublicJobService publicJobService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "List all open jobs with filters (Public)")
    public ResponseEntity<Page<JobSummaryDto>> getOpenJobs(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "Search by job title or skills (in description)") 
            @RequestParam(required = false) String title,
            
            @Parameter(description = "Filter by location") 
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Filter by date posted: '24h', '7d', '30d'") 
            @RequestParam(required = false) String datePosted,
            
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Long candidateId = null;
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
            } catch (Exception e) {
                // Ignore invalid token for public endpoint
            }
        }
        
        return ResponseEntity.ok(publicJobService.getOpenJobs(title, location, datePosted, candidateId, pageable));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get details of a specific open job (Public)")
    public ResponseEntity<JobDetailDto> getJobDetails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long jobId) {
        
        Long candidateId = null;
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                candidateId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
            } catch (Exception e) {
                // Ignore invalid token
            }
        }

        return ResponseEntity.ok(publicJobService.getPublicJobDetails(jobId, candidateId));
    }
}
