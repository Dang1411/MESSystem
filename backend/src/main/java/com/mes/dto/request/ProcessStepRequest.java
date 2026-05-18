package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProcessStepRequest {

    @NotBlank(message = "Mã công đoạn không được để trống")
    private String stepCode;

    @NotBlank(message = "Tên công đoạn không được để trống")
    private String stepName;

    private String description;
    private Boolean isActive = true;
}
