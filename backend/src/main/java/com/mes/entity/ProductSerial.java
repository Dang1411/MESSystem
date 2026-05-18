package com.mes.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_serials")
@Getter @Setter @NoArgsConstructor
public class ProductSerial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_code", nullable = false, unique = true, length = 100)
    private String serialCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Công đoạn hiện tại đang được xử lý
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_step_id")
    private ProcessStep currentStep;

    /**
     * Trạng thái serial:
     * WAITING     - Chờ sản xuất
     * IN_PROGRESS - Đang sản xuất
     * OK          - Đã qua công đoạn OK
     * NG          - Không đạt chất lượng
     * REWORK      - Cần làm lại
     * SCRAP       - Loại bỏ
     * HOLD        - Tạm giữ chờ xử lý
     * FINISHED    - Hoàn thành toàn bộ quy trình
     */
    @Column(nullable = false, length = 20)
    private String status = "WAITING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
