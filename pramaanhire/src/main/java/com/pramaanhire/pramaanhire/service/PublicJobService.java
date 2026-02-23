package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.dto.JobDetailDto;
import com.pramaanhire.pramaanhire.dto.JobQuestionDto;
import com.pramaanhire.pramaanhire.dto.JobSummaryDto;
import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.entity.JobQuestion;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicJobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public Page<JobSummaryDto> getOpenJobs(String title, String location, String datePosted, Long candidateId, Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base filters: OPEN, Active, Not Expired
            predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));
            predicates.add(cb.equal(root.get("isActive"), true));
            
            Predicate deadlineNull = cb.isNull(root.get("applicationDeadline"));
            Predicate deadlineFuture = cb.greaterThan(root.get("applicationDeadline"), LocalDateTime.now());
            predicates.add(cb.or(deadlineNull, deadlineFuture));

            // Dynamic Filters
            if (title != null && !title.isEmpty()) {
                String likePattern = "%" + title.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (location != null && !location.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (datePosted != null && !datePosted.isEmpty()) {
                LocalDateTime fromDate = null;
                if ("24h".equalsIgnoreCase(datePosted)) {
                    fromDate = LocalDateTime.now().minusHours(24);
                } else if ("7d".equalsIgnoreCase(datePosted)) {
                    fromDate = LocalDateTime.now().minusDays(7);
                } else if ("30d".equalsIgnoreCase(datePosted)) {
                    fromDate = LocalDateTime.now().minusDays(30);
                }

                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable)
                .map(job -> {
                    boolean hasApplied = false;
                    if (candidateId != null) {
                        hasApplied = applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidateId);
                    }
                    
                    return JobSummaryDto.builder()
                            .id(job.getId())
                            .title(job.getTitle())
                            .location(job.getLocation())
                            .employmentType(job.getEmploymentType())
                            .status(job.getStatus())
                            .applicationDeadline(job.getApplicationDeadline())
                            .createdAt(job.getCreatedAt())
                            .questionCount(job.getQuestions().size())
                            .hasApplied(hasApplied)
                            .build();
                });
    }

    @Transactional(readOnly = true)
    public JobDetailDto getPublicJobDetails(Long jobId, Long candidateId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Validation: Job must be OPEN, Active, and not expired
        if (job.getStatus() != JobStatus.OPEN || !job.isActive()) {
            throw new RuntimeException("This job is no longer accepting applications");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The application deadline for this job has passed");
        }

        boolean hasApplied = false;
        Long applicationId = null;
        
        if (candidateId != null) {
            Optional<Application> app = applicationRepository.findByJobIdAndCandidateId(jobId, candidateId);
            if (app.isPresent()) {
                hasApplied = true;
                applicationId = app.get().getId();
            }
        }

        return JobDetailDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .applicationDeadline(job.getApplicationDeadline())
                .createdAt(job.getCreatedAt())
                .questions(null) // Hidden for public
                .hasApplied(hasApplied)
                .applicationId(applicationId) // Include ID if applied
                .build();
    }
}
