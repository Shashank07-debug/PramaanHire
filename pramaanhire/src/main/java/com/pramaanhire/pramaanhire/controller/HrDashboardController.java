package com.pramaanhire.pramaanhire.controller;

import com.pramaanhire.pramaanhire.dto.HrDashboardDto;
import com.pramaanhire.pramaanhire.service.HrDashboardService;
import com.pramaanhire.pramaanhire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Tag(name = "HR Dashboard", description = "Endpoints for HR Dashboard Analytics")
@SecurityRequirement(name = "bearerAuth")
public class HrDashboardController {

    private final HrDashboardService hrDashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get comprehensive HR dashboard statistics")
    public ResponseEntity<HrDashboardDto> getDashboardStats(
            @RequestHeader("Authorization") String token) {
        
        String jwt = token.substring(7);
        Long hrId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        
        return ResponseEntity.ok(hrDashboardService.getDashboardStats(hrId));
    }
}
