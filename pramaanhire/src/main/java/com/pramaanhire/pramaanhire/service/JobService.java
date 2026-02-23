package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.dto.*;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.entity.JobQuestion;
import com.pramaanhire.pramaanhire.entity.User;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import com.pramaanhire.pramaanhire.enums.Role;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import com.pramaanhire.pramaanhire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public JobResponse createJob(JobRequest request, Long hrId) {
        User hr = userRepository.findById(hrId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (hr.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can create jobs");
        }

        if (request.getStatus() == JobStatus.OPEN && request.getApplicationDeadline() != null) {
            if (request.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Application deadline must be in the future for OPEN jobs");
            }
        }

        Job job = Job.builder()
                .hr(hr)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .status(request.getStatus())
                .applicationDeadline(request.getApplicationDeadline())
                .isActive(true)
                .questions(new ArrayList<>())
                .build();

        if (request.getQuestions() != null) {
            for (JobQuestionDto qDto : request.getQuestions()) {
                JobQuestion question = JobQuestion.builder()
                        .job(job)
                        .questionText(qDto.getQuestionText())
                        .isMandatory(qDto.getIsMandatory())
                        .maxLength(qDto.getMaxLength())
                        .displayOrder(qDto.getDisplayOrder())
                        .build();
                job.getQuestions().add(question);
            }
        }

        Job savedJob = jobRepository.save(job);

        return JobResponse.builder()
                .jobId(savedJob.getId())
                .message("Job created successfully")
                .build();
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, Long hrId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to update this job");
        }

        // 1. Check for Existing Applications
        long applicationCount = applicationRepository.countByJobId(jobId);

        if (applicationCount > 0) {
            // LOCKED: Cannot update Location or Questions
            
            // Check Location
            if (!job.getLocation().equals(request.getLocation())) {
                throw new RuntimeException("Cannot update Location because applications have already been received.");
            }
            
            // Check Questions (Strict Comparison)
            List<JobQuestionDto> existingQuestions = job.getQuestions().stream()
                    .sorted(Comparator.comparingInt(JobQuestion::getDisplayOrder))
                    .map(q -> JobQuestionDto.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .isMandatory(q.isMandatory())
                            .maxLength(q.getMaxLength())
                            .displayOrder(q.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            
            List<JobQuestionDto> newQuestions = request.getQuestions() != null ? request.getQuestions() : new ArrayList<>();
            newQuestions.sort(Comparator.comparingInt(JobQuestionDto::getDisplayOrder));

            if (!existingQuestions.equals(newQuestions)) {
                throw new RuntimeException("Cannot update Screening Questions because applications have already been received.");
            }
            
        } else {
            // UNLOCKED: Can update Location and Questions
            job.setLocation(request.getLocation());

            // Update Questions (Full Replace Strategy)
            job.getQuestions().clear();
            if (request.getQuestions() != null) {
                for (JobQuestionDto qDto : request.getQuestions()) {
                    JobQuestion question = JobQuestion.builder()
                            .job(job)
                            .questionText(qDto.getQuestionText())
                            .isMandatory(qDto.getIsMandatory())
                            .maxLength(qDto.getMaxLength())
                            .displayOrder(qDto.getDisplayOrder())
                            .build();
                    job.getQuestions().add(question);
                }
            }
        }

        // 2. Update Safe Fields (Always Allowed)
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setEmploymentType(request.getEmploymentType());
        job.setStatus(request.getStatus());
        
        // Validate Deadline if status is OPEN
        if (request.getStatus() == JobStatus.OPEN && request.getApplicationDeadline() != null) {
            if (request.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Application deadline must be in the future for OPEN jobs");
            }
        }
        job.setApplicationDeadline(request.getApplicationDeadline());

        jobRepository.save(job);

        return JobResponse.builder()
                .jobId(job.getId())
                .message("Job updated successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public Page<JobSummaryDto> getJobsByHr(Long hrId, Pageable pageable) {
        return jobRepository.findByHrId(hrId, pageable)
                .map(job -> JobSummaryDto.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .location(job.getLocation())
                        .employmentType(job.getEmploymentType())
                        .status(job.getStatus())
                        .applicationDeadline(job.getApplicationDeadline())
                        .createdAt(job.getCreatedAt())
                        .questionCount(job.getQuestions().size())
                        .applicationCount(applicationRepository.countByJobId(job.getId())) // Populate application count
                        .build());
    }

    @Transactional(readOnly = true)
    public JobDetailDto getJobDetails(Long jobId, Long hrId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to view this job");
        }

        return mapToJobDetailDto(job);
    }

    @Transactional(readOnly = true)
    public JobDetailDto getJobDetailsForCandidate(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Validation for Candidate: Job must be OPEN, Active, and not expired
        if (job.getStatus() != JobStatus.OPEN || !job.isActive()) {
            throw new RuntimeException("This job is no longer accepting applications");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The application deadline for this job has passed");
        }

        return mapToJobDetailDto(job);
    }

    private JobDetailDto mapToJobDetailDto(Job job) {
        return JobDetailDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .applicationDeadline(job.getApplicationDeadline())
                .createdAt(job.getCreatedAt())
                .questions(job.getQuestions().stream()
                        .sorted(Comparator.comparingInt(JobQuestion::getDisplayOrder))
                        .map(q -> JobQuestionDto.builder()
                                .id(q.getId())
                                .questionText(q.getQuestionText())
                                .isMandatory(q.isMandatory())
                                .maxLength(q.getMaxLength())
                                .displayOrder(q.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
