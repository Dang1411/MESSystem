package com.mes.controller;

import com.mes.dto.request.ExecutionRequest;
import com.mes.dto.response.ScanResponse;
import com.mes.service.ScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    @Autowired
    private ScanService scanService;

    /**
     * Quét QR/Barcode để lấy thông tin serial
     */
    @GetMapping("/{serialCode}")
    public ResponseEntity<ScanResponse> scanSerial(@PathVariable String serialCode) {
        return ResponseEntity.ok(scanService.getSerialInfo(serialCode));
    }

    /**
     * Thực thi kết quả công đoạn
     */
    @PostMapping("/execute")
    public ResponseEntity<ScanResponse> executeStep(
            @Valid @RequestBody ExecutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(scanService.executeStep(request, userDetails.getUsername()));
    }
}
