package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response truy xuất nguồn gốc sản phẩm
 */
@Data
public class TraceabilityResponse {
    private Integer serialId;
    private String serialCode;
    private String status;
    // Thông tin lệnh
    private String orderCode;
    private String productCode;
    private String productName;
    private LocalDateTime createdAt;
    // Công đoạn hiện tại
    private String currentStepName;
    // Toàn bộ lịch sử công đoạn
    private List<StepHistory> steps;
    // Danh sách lỗi ghi nhận
    private List<DefectEntry> defects;

    @Data
    public static class StepHistory {
        private String stepCode;
        private String stepName;
        private Integer stepOrder;
        private String result;
        private String operatorName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String notes;
    }

    @Data
    public static class DefectEntry {
        private String defectCode;
        private String defectName;
        private String stepName;
        private String reportedByName;
        private String actionTaken;
        private String notes;
        private LocalDateTime createdAt;
    }
}
