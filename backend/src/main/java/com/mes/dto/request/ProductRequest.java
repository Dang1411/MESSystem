package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProductRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    private String componentType;
    private String description;
    private String status = "ACTIVE";
}
