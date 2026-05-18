package com.mes.repository;

import com.mes.entity.ProductProcessRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductProcessRouteRepository extends JpaRepository<ProductProcessRoute, Integer> {

    // Lấy toàn bộ quy trình của sản phẩm, sắp xếp theo thứ tự
    List<ProductProcessRoute> findByProductIdOrderByStepOrderAsc(Integer productId);

    // Lấy công đoạn theo thứ tự cụ thể của sản phẩm
    Optional<ProductProcessRoute> findByProductIdAndStepOrder(Integer productId, Integer stepOrder);

    // Lấy công đoạn theo process_step_id của sản phẩm
    Optional<ProductProcessRoute> findByProductIdAndProcessStepId(Integer productId, Integer processStepId);

    // Đếm số công đoạn của sản phẩm
    int countByProductId(Integer productId);

    // Xóa toàn bộ quy trình của sản phẩm
    void deleteByProductId(Integer productId);

    // Kiểm tra tồn tại thứ tự
    boolean existsByProductIdAndStepOrder(Integer productId, Integer stepOrder);

    @Query("SELECT MAX(r.stepOrder) FROM ProductProcessRoute r WHERE r.product.id = :productId")
    Optional<Integer> findMaxStepOrderByProductId(Integer productId);
}
