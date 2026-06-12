package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ProductionOrderRequest {
///
    // @NotBlank(message = "Mã lệnh sản xuất không được để trống")
    // private String orderCode;

    @NotNull(message = "Sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "Số lượng kế hoạch không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer plannedQuantity;

    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}
