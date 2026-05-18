package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DefectLogRequest {

    @NotBlank(message = "Mã serial không được để trống")
    private String serialCode;

    @NotNull(message = "Loại lỗi không được để trống")
    private Integer defectId;

    @NotNull(message = "Công đoạn không được để trống")
    private Integer processStepId;

    private String actionTaken;
    private String notes;
}
