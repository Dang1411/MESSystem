// package com.mes.repository;

// import com.mes.entity.ProductionHistory;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface ProductionHistoryRepository extends JpaRepository<ProductionHistory, Integer> {

//     // Lấy lịch sử theo serial, sắp xếp từ mới nhất
//     List<ProductionHistory> findByProductSerialIdOrderByCreatedAtDesc(Integer serialId);

//     // Lấy lịch sử gần nhất của serial tại 1 công đoạn
//     Optional<ProductionHistory> findTopByProductSerialIdAndProcessStepIdOrderByCreatedAtDesc(
//             Integer serialId, Integer stepId);

//     // Kiểm tra serial đã hoàn thành công đoạn chưa
//     @Query("SELECT COUNT(h) > 0 FROM ProductionHistory h " +
//            "WHERE h.productSerial.id = :serialId AND h.processStep.id = :stepId AND h.result = 'OK'")
//     boolean hasCompletedStep(@Param("serialId") Integer serialId, @Param("stepId") Integer stepId);

//     // Thống kê theo kết quả trong khoảng thời gian
//     @Query("SELECT h.result, COUNT(h) FROM ProductionHistory h " +
//            "WHERE h.createdAt >= :from AND h.createdAt <= :to GROUP BY h.result")
//     List<Object[]> countByResultInDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

//     // Thống kê lỗi theo công đoạn
//     @Query("SELECT h.processStep.stepName, COUNT(h) FROM ProductionHistory h " +
//            "WHERE h.result = 'NG' GROUP BY h.processStep.stepName ORDER BY COUNT(h) DESC")
//     List<Object[]> countNgByStep();

//     // Lấy lịch sử theo operator
//     List<ProductionHistory> findByOperatorIdOrderByCreatedAtDesc(Integer operatorId);

//     // Báo cáo theo lệnh sản xuất
//     @Query("SELECT h FROM ProductionHistory h WHERE h.productSerial.productionOrder.id = :orderId ORDER BY h.createdAt DESC")
//     List<ProductionHistory> findByOrderId(@Param("orderId") Integer orderId);

//     // Lấy lịch sử trong khoảng thời gian
//     @Query("SELECT h FROM ProductionHistory h WHERE h.createdAt >= :from AND h.createdAt <= :to")
//     List<ProductionHistory> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

//     // Thống kê sản lượng theo ngày
//     @Query("SELECT CAST(h.createdAt AS date), COUNT(h) FROM ProductionHistory h " +
//            "WHERE h.result = 'OK' AND h.createdAt >= :from AND h.createdAt <= :to " +
//            "GROUP BY CAST(h.createdAt AS date) ORDER BY CAST(h.createdAt AS date)")
//     List<Object[]> countOkByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
// }
package com.mes.repository;

import com.mes.entity.ProductionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionHistoryRepository extends JpaRepository<ProductionHistory, Integer> {

    // Lấy lịch sử theo serial, sắp xếp từ mới nhất
    List<ProductionHistory> findByProductSerialIdOrderByCreatedAtDesc(Integer serialId);

    // Lấy lịch sử gần nhất của serial tại 1 công đoạn
    Optional<ProductionHistory> findTopByProductSerialIdAndProcessStepIdOrderByCreatedAtDesc(
            Integer serialId,
            Integer stepId
    );

    // Kiểm tra serial đã hoàn thành công đoạn chưa
    @Query("""
        SELECT COUNT(h)
        FROM ProductionHistory h
        WHERE h.productSerial.id = :serialId
        AND h.processStep.id = :stepId
        AND h.result = 'OK'
    """)
    Long hasCompletedStep(
            @Param("serialId") Integer serialId,
            @Param("stepId") Integer stepId
    );

    // Thống kê theo kết quả trong khoảng thời gian
    @Query("""
        SELECT h.result, COUNT(h)
        FROM ProductionHistory h
        WHERE h.createdAt >= :from
        AND h.createdAt <= :to
        GROUP BY h.result
    """)
    List<Object[]> countByResultInDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Thống kê lỗi theo công đoạn
    @Query("""
        SELECT h.processStep.stepName, COUNT(h)
        FROM ProductionHistory h
        WHERE h.result = 'NG'
        GROUP BY h.processStep.stepName
        ORDER BY COUNT(h) DESC
    """)
    List<Object[]> countNgByStep();

    // Lấy lịch sử theo operator
    List<ProductionHistory> findByOperatorIdOrderByCreatedAtDesc(Integer operatorId);

    // Báo cáo theo lệnh sản xuất
    @Query("""
        SELECT h
        FROM ProductionHistory h
        WHERE h.productSerial.productionOrder.id = :orderId
        ORDER BY h.createdAt DESC
    """)
    List<ProductionHistory> findByOrderId(@Param("orderId") Integer orderId);

    // Lấy lịch sử trong khoảng thời gian
    @Query("""
        SELECT h
        FROM ProductionHistory h
        WHERE h.createdAt >= :from
        AND h.createdAt <= :to
    """)
    List<ProductionHistory> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Thống kê sản lượng theo ngày
    @Query("""
        SELECT CAST(h.createdAt AS date), COUNT(h)
        FROM ProductionHistory h
        WHERE h.result = 'OK'
        AND h.createdAt >= :from
        AND h.createdAt <= :to
        GROUP BY CAST(h.createdAt AS date)
        ORDER BY CAST(h.createdAt AS date)
    """)
    List<Object[]> countOkByDay(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}