package com.pramaanhire.pramaanhire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDashboardDto {
    private long totalApplications;
    private Map<String, Long> statusBreakdown;
    private List<ApplicationSummaryDto> recentApplications;
    
    // AI Snapshot
    private double averageAiScore;
    private double highestAiScore;
    private double latestAiScore;
    
    // Trend
    private Map<String, Long> applicationsTrend;
}
