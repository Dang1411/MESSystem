package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DefectRequest {

    @NotBlank(message = "Mã lỗi không được để trống")
    private String defectCode;

    @NotBlank(message = "Tên lỗi không được để trống")
    private String defectName;

    private String description;
    private Boolean isActive = true;
}
