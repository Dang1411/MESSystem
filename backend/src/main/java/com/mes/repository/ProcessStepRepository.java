package com.mes.repository;

import com.mes.entity.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessStepRepository extends JpaRepository<ProcessStep, Integer> {

    Optional<ProcessStep> findByStepCode(String stepCode);

    boolean existsByStepCode(String stepCode);

    List<ProcessStep> findByIsActiveTrue();
}
