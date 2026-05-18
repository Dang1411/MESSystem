package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductSerialResponse {
    private Integer id;
    private String serialCode;
    private String status;
    private String orderCode;
    private String productCode;
    private String productName;
    private String currentStepName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
