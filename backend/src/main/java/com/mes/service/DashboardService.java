package com.mes.service;

import com.mes.dto.response.DashboardResponse;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ProductionOrderRepository orderRepository;

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProductionHistoryRepository historyRepository;

    public DashboardResponse getDashboardStats() {
        DashboardResponse res = new DashboardResponse();

        // Thống kê lệnh sản xuất
        res.setTotalOrders(orderRepository.count());
        res.setOrdersCreated(orderRepository.countByStatus("CREATED"));
        res.setOrdersInProgress(orderRepository.countByStatus("IN_PROGRESS"));
        res.setOrdersCompleted(orderRepository.countByStatus("COMPLETED"));
        res.setOrdersCancelled(orderRepository.countByStatus("CANCELLED"));

        // Thống kê sản lượng
        long totalPlanned = Optional.ofNullable(orderRepository.sumPlannedQuantity()).orElse(0L);
        res.setTotalPlanned(totalPlanned);

        long totalSerial = serialRepository.count();
        res.setTotalFinished(serialRepository.countByStatusValue("FINISHED"));
        res.setTotalNg(serialRepository.countByStatusValue("NG"));
        res.setTotalScrap(serialRepository.countByStatusValue("SCRAP"));
        res.setTotalRework(serialRepository.countByStatusValue("REWORK"));
        res.setTotalHold(serialRepository.countByStatusValue("HOLD"));
        res.setTotalWaiting(serialRepository.countByStatusValue("WAITING"));

        long finished = res.getTotalFinished();
        long ng = res.getTotalNg();

        // Tỷ lệ hoàn thành và lỗi
        if (totalSerial > 0) {
            res.setCompletionRate(Math.round((double) finished / totalSerial * 1000.0) / 10.0);
            res.setNgRate(Math.round((double) ng / totalSerial * 1000.0) / 10.0);
        }

        // Biểu đồ sản lượng 7 ngày gần nhất
        LocalDateTime from = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Object[]> okByDay = historyRepository.countOkByDay(from, to);
        
        // Tạo map với đủ 7 ngày (kể cả ngày không có dữ liệu)
        Map<String, Long> dayMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).format(formatter);
            dayMap.put(day, 0L);
        }
        for (Object[] row : okByDay) {
            try {
                String day = row[0].toString().substring(5, 10)
                        .replace("-", "/");
                // Convert MM/dd to dd/MM
                String[] parts = day.split("/");
                if (parts.length == 2) {
                    String formatted = parts[1] + "/" + parts[0];
                    dayMap.put(formatted, ((Number) row[1]).longValue());
                }
            } catch (Exception ignored) {}
        }

        List<DashboardResponse.ChartPoint> productionChart = dayMap.entrySet().stream()
                .map(e -> {
                    DashboardResponse.ChartPoint pt = new DashboardResponse.ChartPoint();
                    pt.setDate(e.getKey());
                    pt.setCount(e.getValue());
                    return pt;
                }).collect(Collectors.toList());
        res.setProductionChart(productionChart);

        // Biểu đồ chất lượng
        Map<String, Long> qualityChart = new LinkedHashMap<>();
        qualityChart.put("FINISHED", res.getTotalFinished());
        qualityChart.put("NG", res.getTotalNg());
        qualityChart.put("REWORK", res.getTotalRework());
        qualityChart.put("SCRAP", res.getTotalScrap());
        qualityChart.put("HOLD", res.getTotalHold());
        qualityChart.put("WAITING", res.getTotalWaiting());
        res.setQualityChart(qualityChart);

        // Top công đoạn phát sinh lỗi nhiều nhất
        List<Object[]> ngByStep = historyRepository.countNgByStep();
        List<DashboardResponse.StepDefectStat> topDefects = ngByStep.stream()
                .limit(5)
                .map(row -> {
                    DashboardResponse.StepDefectStat stat = new DashboardResponse.StepDefectStat();
                    stat.setStepName((String) row[0]);
                    stat.setNgCount(((Number) row[1]).longValue());
                    return stat;
                }).collect(Collectors.toList());
        res.setTopDefectSteps(topDefects);

        return res;
    }
}
