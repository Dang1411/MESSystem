package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionOrderResponse {
    private Integer id;
    private String orderCode;
    private Integer productId;
    private String productCode;
    private String productName;
    private Integer plannedQuantity;
    private Integer completedQuantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String createdByName;
    private LocalDateTime createdAt;
    private String notes;
    // Thống kê nhanh
    private Long serialWaiting;
    private Long serialInProgress;
    private Long serialFinished;
    private Long serialNg;
    private Long serialScrap;
}
