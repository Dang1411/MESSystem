package com.mes.controller;

import com.mes.dto.request.ProcessStepRequest;
import com.mes.dto.response.ProcessStepResponse;
import com.mes.service.ProcessStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/process-steps")
public class ProcessStepController {

    @Autowired
    private ProcessStepService processStepService;

    @GetMapping
    public ResponseEntity<List<ProcessStepResponse>> getAllSteps() {
        return ResponseEntity.ok(processStepService.getAllSteps());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProcessStepResponse>> getActiveSteps() {
        return ResponseEntity.ok(processStepService.getActiveSteps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessStepResponse> getStepById(@PathVariable Integer id) {
        return ResponseEntity.ok(processStepService.getStepById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProcessStepResponse> createStep(@Valid @RequestBody ProcessStepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processStepService.createStep(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProcessStepResponse> updateStep(
            @PathVariable Integer id, @Valid @RequestBody ProcessStepRequest request) {
        return ResponseEntity.ok(processStepService.updateStep(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteStep(@PathVariable Integer id) {
        processStepService.deleteStep(id);
        return ResponseEntity.noContent().build();
    }
}
