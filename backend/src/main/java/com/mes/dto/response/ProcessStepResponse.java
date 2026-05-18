package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessStepResponse {
    private Integer id;
    private String stepCode;
    private String stepName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
