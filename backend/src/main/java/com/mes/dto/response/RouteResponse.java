package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RouteResponse {
    private Integer id;
    private Integer productId;
    private Integer processStepId;
    private String stepCode;
    private String stepName;
    private Integer stepOrder;
    private Boolean isMandatory;
    private LocalDateTime createdAt;
}
