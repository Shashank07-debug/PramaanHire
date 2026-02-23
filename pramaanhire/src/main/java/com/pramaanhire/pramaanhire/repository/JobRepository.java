package com.pramaanhire.pramaanhire.repository;

import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByHrId(Long hrId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.isActive = true AND (j.applicationDeadline IS NULL OR j.applicationDeadline > :now)")
    Page<Job> findOpenJobs(@Param("status") JobStatus status, @Param("now") LocalDateTime now, Pageable pageable);
}
