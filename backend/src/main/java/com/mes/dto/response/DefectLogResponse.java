package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DefectLogResponse {
    private Integer id;
    private String serialCode;
    private String defectCode;
    private String defectName;
    private String stepCode;
    private String stepName;
    private String reportedByName;
    private String actionTaken;
    private String notes;
    private LocalDateTime createdAt;
}
