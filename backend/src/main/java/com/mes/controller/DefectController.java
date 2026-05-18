package com.mes.controller;

import com.mes.dto.request.DefectLogRequest;
import com.mes.dto.request.DefectRequest;
import com.mes.dto.response.DefectLogResponse;
import com.mes.dto.response.DefectResponse;
import com.mes.service.DefectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/defects")
public class DefectController {

    @Autowired
    private DefectService defectService;

    // ===== DANH MỤC LỖI =====

    @GetMapping
    public ResponseEntity<List<DefectResponse>> getAllDefects() {
        return ResponseEntity.ok(defectService.getAllDefects());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DefectResponse>> getActiveDefects() {
        return ResponseEntity.ok(defectService.getActiveDefects());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','QC')")
    public ResponseEntity<DefectResponse> createDefect(@Valid @RequestBody DefectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defectService.createDefect(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','QC')")
    public ResponseEntity<DefectResponse> updateDefect(
            @PathVariable Integer id, @Valid @RequestBody DefectRequest request) {
        return ResponseEntity.ok(defectService.updateDefect(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteDefect(@PathVariable Integer id) {
        defectService.deleteDefect(id);
        return ResponseEntity.noContent().build();
    }

    // ===== NHẬT KÝ LỖI =====

    @GetMapping("/logs")
    public ResponseEntity<List<DefectLogResponse>> getDefectLogs(
            @RequestParam(required = false) Integer stepId,
            @RequestParam(required = false) Integer defectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(defectService.getDefectLogs(stepId, defectId, from, to));
    }

    @GetMapping("/logs/serial/{serialCode}")
    public ResponseEntity<List<DefectLogResponse>> getLogsBySerial(@PathVariable String serialCode) {
        return ResponseEntity.ok(defectService.getDefectLogsBySerial(serialCode));
    }

    @PostMapping("/logs")
    @PreAuthorize("hasAnyRole('SUPERVISOR','QC','OPERATOR')")
    public ResponseEntity<DefectLogResponse> createDefectLog(
            @Valid @RequestBody DefectLogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(defectService.createDefectLog(request, userDetails.getUsername()));
    }
}
