package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DefectResponse {
    private Integer id;
    private String defectCode;
    private String defectName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
