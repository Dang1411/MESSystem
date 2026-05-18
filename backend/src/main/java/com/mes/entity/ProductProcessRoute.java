package com.mes.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_process_routes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "step_order"}))
@Getter @Setter @NoArgsConstructor
public class ProductProcessRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "process_step_id", nullable = false)
    private ProcessStep processStep;

    // Thứ tự công đoạn (1, 2, 3, ...)
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
