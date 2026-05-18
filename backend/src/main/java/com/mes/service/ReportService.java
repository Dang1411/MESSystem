package com.mes.service;

import com.mes.entity.ProductSerial;
import com.mes.entity.ProductionHistory;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ProductionOrderRepository orderRepository;

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProductionHistoryRepository historyRepository;

    /**
     * Báo cáo sản lượng theo lệnh sản xuất
     */
    public List<Map<String, Object>> getProductionReport(LocalDateTime from, LocalDateTime to,
                                                          String status, Integer productId) {
        return orderRepository.searchOrders(null, status, productId)
                .stream()
                .filter(o -> from == null || o.getCreatedAt().isAfter(from))
                .filter(o -> to == null || o.getCreatedAt().isBefore(to))
                .map(o -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("orderId", o.getId());
                    row.put("orderCode", o.getOrderCode());
                    row.put("productCode", o.getProduct().getProductCode());
                    row.put("productName", o.getProduct().getProductName());
                    row.put("plannedQuantity", o.getPlannedQuantity());
                    row.put("completedQuantity", o.getCompletedQuantity());
                    int rate = o.getPlannedQuantity() > 0
                            ? (int) Math.round(o.getCompletedQuantity() * 100.0 / o.getPlannedQuantity())
                            : 0;
                    row.put("completionRate", rate);
                    row.put("status", o.getStatus());
                    row.put("startDate", o.getStartDate());
                    row.put("endDate", o.getEndDate());
                    row.put("createdAt", o.getCreatedAt());
                    row.put("createdBy", o.getCreatedBy().getFullName());
                    return row;
                })
                .collect(Collectors.toList());
    }

    /**
     * Báo cáo chất lượng theo công đoạn
     */
    public List<Map<String, Object>> getQualityReport(LocalDateTime from, LocalDateTime to) {
        LocalDateTime f = from != null ? from : LocalDateTime.now().minusDays(30);
        LocalDateTime t = to != null ? to : LocalDateTime.now();

        List<ProductionHistory> histories = historyRepository.findByDateRange(f, t);

        // Nhóm theo công đoạn
        Map<Integer, List<ProductionHistory>> byStep = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getProcessStep().getId()));

        List<Map<String, Object>> report = new ArrayList<>();
        for (Map.Entry<Integer, List<ProductionHistory>> entry : byStep.entrySet()) {
            List<ProductionHistory> sh = entry.getValue();
            ProductionHistory first = sh.get(0);
            long okCount = sh.stream().filter(h -> "OK".equals(h.getResult())).count();
            long ngCount = sh.stream().filter(h -> "NG".equals(h.getResult())).count();
            long reworkCount = sh.stream().filter(h -> "REWORK".equals(h.getResult())).count();
            long scrapCount = sh.stream().filter(h -> "SCRAP".equals(h.getResult())).count();
            long total = sh.size();
            int ngRate = total > 0 ? (int) Math.round(ngCount * 100.0 / total) : 0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("stepId", first.getProcessStep().getId());
            row.put("stepCode", first.getProcessStep().getStepCode());
            row.put("stepName", first.getProcessStep().getStepName());
            row.put("total", total);
            row.put("okCount", okCount);
            row.put("ngCount", ngCount);
            row.put("reworkCount", reworkCount);
            row.put("scrapCount", scrapCount);
            row.put("ngRate", ngRate);
            report.add(row);
        }
        // Sắp xếp theo tên công đoạn
        report.sort(Comparator.comparing(r -> (String) r.get("stepName")));
        return report;
    }

    /**
     * Báo cáo serial theo lệnh sản xuất
     */
    public List<Map<String, Object>> getSerialReport(String serialCode, Integer orderId) {
        List<ProductSerial> serials;
        if (serialCode != null && !serialCode.isEmpty()) {
            serials = serialRepository.findBySerialCode(serialCode)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else if (orderId != null) {
            serials = serialRepository.findByProductionOrderId(orderId);
        } else {
            serials = serialRepository.findAll();
        }

        // Nhóm theo lệnh sản xuất
        Map<Integer, List<ProductSerial>> byOrder = serials.stream()
                .collect(Collectors.groupingBy(s -> s.getProductionOrder().getId()));

        List<Map<String, Object>> report = new ArrayList<>();
        for (Map.Entry<Integer, List<ProductSerial>> entry : byOrder.entrySet()) {
            List<ProductSerial> os = entry.getValue();
            ProductSerial first = os.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderId", first.getProductionOrder().getId());
            row.put("orderCode", first.getProductionOrder().getOrderCode());
            row.put("productName", first.getProduct().getProductName());
            row.put("waitingCount", os.stream().filter(s -> "WAITING".equals(s.getStatus())).count());
            row.put("inProgressCount", os.stream().filter(s -> "IN_PROGRESS".equals(s.getStatus())).count());
            row.put("finishedCount", os.stream().filter(s -> "FINISHED".equals(s.getStatus())).count());
            row.put("ngCount", os.stream().filter(s -> "NG".equals(s.getStatus())).count());
            row.put("holdCount", os.stream().filter(s -> "HOLD".equals(s.getStatus())).count());
            row.put("scrapCount", os.stream().filter(s -> "SCRAP".equals(s.getStatus())).count());
            row.put("totalCount", (long) os.size());
            report.add(row);
        }
        report.sort(Comparator.comparing(r -> (String) r.get("orderCode")));
        return report;
    }
}

