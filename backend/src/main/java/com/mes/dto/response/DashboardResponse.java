package com.mes.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Response cho Dashboard thống kê
 */
@Data
public class DashboardResponse {
    // Thống kê lệnh sản xuất
    private long totalOrders;
    private long ordersCreated;
    private long ordersInProgress;
    private long ordersCompleted;
    private long ordersCancelled;

    // Thống kê sản lượng
    private long totalPlanned;
    private long totalFinished;
    private long totalNg;
    private long totalScrap;
    private long totalRework;
    private long totalHold;
    private long totalWaiting;

    // Tỷ lệ
    private double completionRate;
    private double ngRate;

    // Biểu đồ sản lượng theo ngày (key = ngày, value = số lượng OK)
    private List<ChartPoint> productionChart;

    // Biểu đồ chất lượng
    private Map<String, Long> qualityChart;

    // Top công đoạn phát sinh lỗi
    private List<StepDefectStat> topDefectSteps;

    @Data
    public static class ChartPoint {
        private String date;
        private long count;
    }

    @Data
    public static class StepDefectStat {
        private String stepName;
        private long ngCount;
    }
}
