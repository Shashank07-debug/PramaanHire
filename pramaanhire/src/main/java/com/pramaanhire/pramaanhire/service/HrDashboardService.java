package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.dto.HrDashboardDto;
import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.enums.ApplicationStatus;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HrDashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public HrDashboardDto getDashboardStats(Long hrId) {
        // 1. Fetch all jobs created by this HR
        List<Job> jobs = jobRepository.findAll().stream()
                .filter(job -> job.getHr().getId().equals(hrId))
                .collect(Collectors.toList());
        
        List<Long> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());

        // 2. Fetch all applications for these jobs
        List<Application> applications = new ArrayList<>();
        if (!jobIds.isEmpty()) {
            applications = applicationRepository.findAll().stream()
                    .filter(app -> jobIds.contains(app.getJob().getId()))
                    .collect(Collectors.toList());
        }

        // 3. Calculate KPIs
        long openJobs = jobs.stream().filter(j -> j.getStatus() == JobStatus.OPEN).count();
        long totalApps = applications.size();
        long shortlisted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        long hired = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count();
        long underReview = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count();
        long rejected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        // 4. Status Distribution (Donut Chart)
        Map<String, Long> statusDist = new HashMap<>();
        statusDist.put("Submitted", applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED).count());
        statusDist.put("Under Review", underReview);
        statusDist.put("Shortlisted", shortlisted);
        statusDist.put("Hired", hired);
        statusDist.put("Rejected", rejected);

        // 5. Application Trend (Last 30 Days)
        Map<String, Long> trend = new TreeMap<>(); // Sorted by date
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String key = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            long count = applications.stream()
                    .filter(a -> a.getSubmittedAt().toLocalDate().equals(date))
                    .count();
            trend.put(key, count);
        }

        // 6. AI Snapshot
        DoubleSummaryStatistics aiStats = applications.stream()
                .map(Application::getAiScore)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();

        // 7. Recent Activity (Last 5)
        List<HrDashboardDto.RecentActivityDto> activities = applications.stream()
                .sorted(Comparator.comparing(Application::getSubmittedAt).reversed())
                .limit(5)
                .map(app -> HrDashboardDto.RecentActivityDto.builder()
                        .description(app.getCandidate().getFullName() + " applied for " + app.getJob().getTitle())
                        .timeAgo(calculateTimeAgo(app.getSubmittedAt()))
                        .type("APPLICATION")
                        .build())
                .collect(Collectors.toList());

        return HrDashboardDto.builder()
                .openJobsCount(openJobs)
                .totalApplicationsCount(totalApps)
                .shortlistedCount(shortlisted)
                .hiredCount(hired)
                .underReviewCount(underReview)
                .rejectedCount(rejected)
                .statusDistribution(statusDist)
                .applicationsTrend(trend)
                .averageAiScore(aiStats.getCount() > 0 ? Math.round(aiStats.getAverage() * 10.0) / 10.0 : 0.0)
                .highestAiScore(aiStats.getCount() > 0 ? aiStats.getMax() : 0.0)
                .lowestAiScore(aiStats.getCount() > 0 ? aiStats.getMin() : 0.0)
                .recentActivities(activities)
                .build();
    }

    private String calculateTimeAgo(java.time.LocalDateTime dateTime) {
        long minutes = ChronoUnit.MINUTES.between(dateTime, java.time.LocalDateTime.now());
        if (minutes < 60) return minutes + "m ago";
        long hours = ChronoUnit.HOURS.between(dateTime, java.time.LocalDateTime.now());
        if (hours < 24) return hours + "h ago";
        long days = ChronoUnit.DAYS.between(dateTime, java.time.LocalDateTime.now());
        return days + "d ago";
    }
}
