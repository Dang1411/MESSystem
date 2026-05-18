package com.mes.controller;

import com.mes.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('SUPERVISOR')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Báo cáo sản lượng theo lệnh sản xuất
     */
    @GetMapping("/production")
    public ResponseEntity<List<Map<String, Object>>> getProductionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer productId) {
        return ResponseEntity.ok(reportService.getProductionReport(from, to, status, productId));
    }

    /**
     * Báo cáo chất lượng theo kết quả
     */
    @GetMapping("/quality")
    public ResponseEntity<List<Map<String, Object>>> getQualityReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getQualityReport(from, to));
    }

    /**
     * Báo cáo lịch sử theo serial
     */
    @GetMapping("/serials")
    public ResponseEntity<List<Map<String, Object>>> getSerialReport(
            @RequestParam(required = false) String serialCode,
            @RequestParam(required = false) Integer orderId) {
        return ResponseEntity.ok(reportService.getSerialReport(serialCode, orderId));
    }
}
