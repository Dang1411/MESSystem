package com.mes.controller;

import com.mes.dto.response.TraceabilityResponse;
import com.mes.service.TraceabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traceability")
public class TraceabilityController {

    @Autowired
    private TraceabilityService traceabilityService;

    @GetMapping("/{serialCode}")
    public ResponseEntity<TraceabilityResponse> traceSerial(@PathVariable String serialCode) {
        return ResponseEntity.ok(traceabilityService.traceSerial(serialCode));
    }
}
