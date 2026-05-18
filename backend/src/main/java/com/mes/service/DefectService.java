package com.mes.service;

import com.mes.dto.request.DefectLogRequest;
import com.mes.dto.request.DefectRequest;
import com.mes.dto.response.DefectLogResponse;
import com.mes.dto.response.DefectResponse;
import com.mes.entity.*;
import com.mes.exception.MesException;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefectService {

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private DefectLogRepository defectLogRepository;

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProcessStepRepository processStepRepository;

    @Autowired
    private UserRepository userRepository;

    // ===== DEFECT CATALOG =====

    public List<DefectResponse> getAllDefects() {
        return defectRepository.findAll().stream().map(this::toDefectResponse).collect(Collectors.toList());
    }

    public List<DefectResponse> getActiveDefects() {
        return defectRepository.findByIsActiveTrue().stream().map(this::toDefectResponse).collect(Collectors.toList());
    }

    @Transactional
    public DefectResponse createDefect(DefectRequest request) {
        if (defectRepository.existsByDefectCode(request.getDefectCode())) {
            throw new MesException("Mã lỗi '" + request.getDefectCode() + "' đã tồn tại");
        }
        Defect defect = new Defect();
        defect.setDefectCode(request.getDefectCode());
        defect.setDefectName(request.getDefectName());
        defect.setDescription(request.getDescription());
        defect.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return toDefectResponse(defectRepository.save(defect));
    }

    @Transactional
    public DefectResponse updateDefect(Integer id, DefectRequest request) {
        Defect defect = defectRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy loại lỗi", HttpStatus.NOT_FOUND));
        defect.setDefectCode(request.getDefectCode());
        defect.setDefectName(request.getDefectName());
        defect.setDescription(request.getDescription());
        if (request.getIsActive() != null) defect.setIsActive(request.getIsActive());
        return toDefectResponse(defectRepository.save(defect));
    }

    @Transactional
    public void deleteDefect(Integer id) {
        Defect defect = defectRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy loại lỗi", HttpStatus.NOT_FOUND));
        defectRepository.delete(defect);
    }

    // ===== DEFECT LOGS =====

    public List<DefectLogResponse> getDefectLogs(Integer stepId, Integer defectId,
                                                   LocalDateTime from, LocalDateTime to) {
        return defectLogRepository.searchLogs(stepId, defectId, from, to)
                .stream().map(this::toLogResponse).collect(Collectors.toList());
    }

    public List<DefectLogResponse> getDefectLogsBySerial(String serialCode) {
        ProductSerial serial = serialRepository.findBySerialCode(serialCode)
                .orElseThrow(() -> new MesException("Không tìm thấy serial"));
        return defectLogRepository.findByProductSerialIdOrderByCreatedAtDesc(serial.getId())
                .stream().map(this::toLogResponse).collect(Collectors.toList());
    }

    @Transactional
    public DefectLogResponse createDefectLog(DefectLogRequest request, String reporterUsername) {
        ProductSerial serial = serialRepository.findBySerialCode(request.getSerialCode())
                .orElseThrow(() -> new MesException("Không tìm thấy sản phẩm"));
        Defect defect = defectRepository.findById(request.getDefectId())
                .orElseThrow(() -> new MesException("Không tìm thấy loại lỗi"));
        ProcessStep step = processStepRepository.findById(request.getProcessStepId())
                .orElseThrow(() -> new MesException("Không tìm thấy công đoạn"));
        User reporter = userRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new MesException("Không tìm thấy người dùng"));

        DefectLog log = new DefectLog();
        log.setProductSerial(serial);
        log.setDefect(defect);
        log.setProcessStep(step);
        log.setReportedBy(reporter);
        log.setActionTaken(request.getActionTaken());
        log.setNotes(request.getNotes());

        return toLogResponse(defectLogRepository.save(log));
    }

    // --- Helpers ---
    private DefectResponse toDefectResponse(Defect d) {
        DefectResponse res = new DefectResponse();
        res.setId(d.getId());
        res.setDefectCode(d.getDefectCode());
        res.setDefectName(d.getDefectName());
        res.setDescription(d.getDescription());
        res.setIsActive(d.getIsActive());
        res.setCreatedAt(d.getCreatedAt());
        return res;
    }

    private DefectLogResponse toLogResponse(DefectLog d) {
        DefectLogResponse res = new DefectLogResponse();
        res.setId(d.getId());
        res.setSerialCode(d.getProductSerial().getSerialCode());
        res.setDefectCode(d.getDefect().getDefectCode());
        res.setDefectName(d.getDefect().getDefectName());
        res.setStepCode(d.getProcessStep().getStepCode());
        res.setStepName(d.getProcessStep().getStepName());
        res.setReportedByName(d.getReportedBy().getFullName());
        res.setActionTaken(d.getActionTaken());
        res.setNotes(d.getNotes());
        res.setCreatedAt(d.getCreatedAt());
        return res;
    }
}
