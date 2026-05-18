package com.mes.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_histories")
@Getter @Setter @NoArgsConstructor
public class ProductionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "serial_id", nullable = false)
    private ProductSerial productSerial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "process_step_id", nullable = false)
    private ProcessStep processStep;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Kết quả công đoạn: OK, NG, REWORK, SCRAP, HOLD
     */
    @Column(length = 20)
    private String result;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
