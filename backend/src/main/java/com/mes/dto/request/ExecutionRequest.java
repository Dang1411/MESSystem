package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ExecutionRequest {

    @NotBlank(message = "Mã serial không được để trống")
    private String serialCode;

    @NotNull(message = "ID công đoạn không được để trống")
    private Integer stepId;

    @NotBlank(message = "Kết quả không được để trống")
    private String result; // OK, NG, REWORK, SCRAP, HOLD

    private String notes;

    // ID lỗi (khi result = NG)
    private Integer defectId;

    // ID công đoạn cần rework về (khi result = REWORK)
    private Integer reworkStepId;
}
