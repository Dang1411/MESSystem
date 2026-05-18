package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response khi quét QR/Barcode sản phẩm
 */
@Data
public class ScanResponse {
    private Integer serialId;
    private String serialCode;
    private String status;
    // Thông tin lệnh sản xuất
    private String orderCode;
    private Integer orderId;
    // Thông tin sản phẩm
    private String productCode;
    private String productName;
    // Công đoạn hiện tại
    private Integer currentStepId;
    private String currentStepCode;
    private String currentStepName;
    private Integer currentStepOrder;
    // Công đoạn tiếp theo
    private Integer nextStepId;
    private String nextStepCode;
    private String nextStepName;
    // Tổng số công đoạn
    private Integer totalSteps;
    // Lịch sử xử lý
    private List<HistoryItem> history;
    private LocalDateTime updatedAt;

    @Data
    public static class HistoryItem {
        private Integer id;
        private String stepCode;
        private String stepName;
        private String result;
        private String operatorName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String notes;
    }
}
