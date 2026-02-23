package com.pramaanhire.pramaanhire.repository;

import com.pramaanhire.pramaanhire.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId); // Added to fetch ID
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);
    List<Application> findByIsAiProcessedFalse();
    long countByJobId(Long jobId);
    Page<Application> findByJobId(Long jobId, Pageable pageable);
}
