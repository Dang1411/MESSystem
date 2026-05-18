package com.mes.service;

import com.mes.dto.response.TraceabilityResponse;
import com.mes.entity.*;
import com.mes.exception.MesException;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TraceabilityService {

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProductionHistoryRepository historyRepository;

    @Autowired
    private DefectLogRepository defectLogRepository;

    @Autowired
    private ProductProcessRouteRepository routeRepository;

    /**
     * Truy xuất toàn bộ lịch sử sản xuất của một serial
     */
    public TraceabilityResponse traceSerial(String serialCode) {
        ProductSerial serial = serialRepository.findBySerialCode(serialCode)
                .orElseThrow(() -> new MesException("Không tìm thấy sản phẩm với mã: " + serialCode));

        TraceabilityResponse res = new TraceabilityResponse();
        res.setSerialId(serial.getId());
        res.setSerialCode(serial.getSerialCode());
        res.setStatus(serial.getStatus());
        res.setOrderCode(serial.getProductionOrder().getOrderCode());
        res.setProductCode(serial.getProduct().getProductCode());
        res.setProductName(serial.getProduct().getProductName());
        res.setCreatedAt(serial.getCreatedAt());
        res.setCurrentStepName(serial.getCurrentStep() != null ? serial.getCurrentStep().getStepName() : null);

        // Lịch sử từng công đoạn
        List<ProductionHistory> histories = historyRepository
                .findByProductSerialIdOrderByCreatedAtDesc(serial.getId());

        List<TraceabilityResponse.StepHistory> steps = histories.stream().map(h -> {
            TraceabilityResponse.StepHistory sh = new TraceabilityResponse.StepHistory();
            sh.setStepCode(h.getProcessStep().getStepCode());
            sh.setStepName(h.getProcessStep().getStepName());
            // Lấy thứ tự công đoạn
            routeRepository.findByProductIdAndProcessStepId(
                    serial.getProduct().getId(), h.getProcessStep().getId())
                    .ifPresent(r -> sh.setStepOrder(r.getStepOrder()));
            sh.setResult(h.getResult());
            sh.setOperatorName(h.getOperator().getFullName());
            sh.setStartTime(h.getStartTime());
            sh.setEndTime(h.getEndTime());
            sh.setNotes(h.getNotes());
            return sh;
        }).collect(Collectors.toList());
        res.setSteps(steps);

        // Danh sách lỗi
        List<DefectLog> defectLogs = defectLogRepository
                .findByProductSerialIdOrderByCreatedAtDesc(serial.getId());

        List<TraceabilityResponse.DefectEntry> defects = defectLogs.stream().map(d -> {
            TraceabilityResponse.DefectEntry de = new TraceabilityResponse.DefectEntry();
            de.setDefectCode(d.getDefect().getDefectCode());
            de.setDefectName(d.getDefect().getDefectName());
            de.setStepName(d.getProcessStep().getStepName());
            de.setReportedByName(d.getReportedBy().getFullName());
            de.setActionTaken(d.getActionTaken());
            de.setNotes(d.getNotes());
            de.setCreatedAt(d.getCreatedAt());
            return de;
        }).collect(Collectors.toList());
        res.setDefects(defects);

        return res;
    }
}
