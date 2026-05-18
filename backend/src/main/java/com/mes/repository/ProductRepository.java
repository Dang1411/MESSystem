package com.mes.repository;

import com.mes.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.productCode) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(p.productName) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<Product> searchProducts(String keyword);

    List<Product> findByStatus(String status);
}
