package com.mes.repository;

import com.mes.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Integer> {

    Optional<ProductSerial> findBySerialCode(String serialCode);

    boolean existsBySerialCode(String serialCode);

    List<ProductSerial> findByProductionOrderId(Integer orderId);

    List<ProductSerial> findByStatus(String status);

    long countByProductionOrderIdAndStatus(Integer orderId, String status);

    long countByProductionOrderId(Integer orderId);

    // Thống kê cho dashboard
    @Query("SELECT s.status, COUNT(s) FROM ProductSerial s GROUP BY s.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT COUNT(s) FROM ProductSerial s WHERE s.status = :status")
    long countByStatusValue(@Param("status") String status);

    // Lấy serials cần QC (NG hoặc HOLD)
    @Query("SELECT s FROM ProductSerial s WHERE s.status IN ('NG','HOLD') ORDER BY s.updatedAt DESC")
    List<ProductSerial> findPendingQC();

    // Đếm theo order và trạng thái
    @Query("SELECT s.status, COUNT(s) FROM ProductSerial s WHERE s.productionOrder.id = :orderId GROUP BY s.status")
    List<Object[]> countByOrderGroupedByStatus(@Param("orderId") Integer orderId);
}
