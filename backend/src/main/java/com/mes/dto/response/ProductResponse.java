package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Integer id;
    private String productCode;
    private String productName;
    private String componentType;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
