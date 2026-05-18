package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RouteRequest {
    @NotNull(message = "ID công đoạn không được để trống")
    private Integer processStepId;

    @NotNull(message = "Thứ tự không được để trống")
    private Integer stepOrder;

    private Boolean isMandatory = true;
}
