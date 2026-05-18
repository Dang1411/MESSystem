package com.mes.repository;

import com.mes.entity.Defect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Integer> {

    Optional<Defect> findByDefectCode(String defectCode);

    boolean existsByDefectCode(String defectCode);

    List<Defect> findByIsActiveTrue();
}
