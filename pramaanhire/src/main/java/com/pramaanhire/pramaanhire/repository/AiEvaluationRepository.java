package com.pramaanhire.pramaanhire.repository;

import com.pramaanhire.pramaanhire.entity.AiEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiEvaluationRepository extends JpaRepository<AiEvaluation, Long> {
}
