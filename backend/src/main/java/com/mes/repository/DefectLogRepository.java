package com.mes.repository;

import com.mes.entity.DefectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DefectLogRepository extends JpaRepository<DefectLog, Integer> {

    List<DefectLog> findByProductSerialIdOrderByCreatedAtDesc(Integer serialId);

    List<DefectLog> findByProcessStepIdOrderByCreatedAtDesc(Integer stepId);

    @Query("SELECT d FROM DefectLog d WHERE " +
           "(:stepId IS NULL OR d.processStep.id = :stepId) " +
           "AND (:defectId IS NULL OR d.defect.id = :defectId) " +
           "AND (:from IS NULL OR d.createdAt >= :from) " +
           "AND (:to IS NULL OR d.createdAt <= :to) " +
           "ORDER BY d.createdAt DESC")
    List<DefectLog> searchLogs(@Param("stepId") Integer stepId,
                                @Param("defectId") Integer defectId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    // Thống kê top lỗi phổ biến
    @Query("SELECT d.defect.defectName, COUNT(d) FROM DefectLog d GROUP BY d.defect.defectName ORDER BY COUNT(d) DESC")
    List<Object[]> countByDefectName();
}
