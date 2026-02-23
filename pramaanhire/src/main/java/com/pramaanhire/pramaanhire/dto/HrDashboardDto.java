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
public class HrDashboardDto {
    // KPI Cards
    private long openJobsCount;
    private long totalApplicationsCount;
    private long shortlistedCount;
    private long hiredCount;
    private long underReviewCount;
    private long rejectedCount;

    // Charts
    private Map<String, Long> statusDistribution; // For Donut Chart
    private Map<String, Long> applicationsTrend;  // For Line Chart (Date -> Count)

    // AI Snapshot
    private double averageAiScore;
    private double highestAiScore;
    private double lowestAiScore;

    // Recent Activity
    private List<RecentActivityDto> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDto {
        private String description; // e.g., "John Doe applied for Java Dev"
        private String timeAgo;     // e.g., "2 hours ago"
        private String type;        // APPLICATION, STATUS_CHANGE, JOB_POSTED
    }
}
