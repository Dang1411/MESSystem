package com.mes.repository;

import com.mes.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Integer> {

    Optional<ProductionOrder> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    List<ProductionOrder> findByStatus(String status);

    List<ProductionOrder> findByProductId(Integer productId);

    @Query("SELECT o FROM ProductionOrder o WHERE " +
           "(:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:productId IS NULL OR o.product.id = :productId) " +
           "ORDER BY o.createdAt DESC")
    List<ProductionOrder> searchOrders(@Param("keyword") String keyword,
                                        @Param("status") String status,
                                        @Param("productId") Integer productId);

    // Thống kê dashboard
    long countByStatus(String status);

    @Query("SELECT SUM(o.plannedQuantity) FROM ProductionOrder o")
    Long sumPlannedQuantity();

    @Query("SELECT SUM(o.completedQuantity) FROM ProductionOrder o")
    Long sumCompletedQuantity();

    // Lấy orders trong khoảng thời gian
    @Query("SELECT o FROM ProductionOrder o WHERE o.createdAt >= :from AND o.createdAt <= :to ORDER BY o.createdAt ASC")
    List<ProductionOrder> findByDateRange(@Param("from") java.time.LocalDateTime from,
                                           @Param("to") java.time.LocalDateTime to);





                                           
    //tìm mã lớn nhất trong ngày
    @Query("SELECT COUNT(o) FROM ProductionOrder o WHERE o.status IN ('CREATED','IN_PROGRESS')")
    long countActiveOrders();
    @Query("""
    SELECT MAX(o.orderCode)
    FROM ProductionOrder o
    WHERE o.product.productCode = :productCode
      AND o.orderCode LIKE :prefix%
    """)
    String findMaxOrderCodeByPrefix(
        @Param("productCode") String productCode,
        @Param("prefix") String prefix
);
}
