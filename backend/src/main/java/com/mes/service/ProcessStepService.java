package com.mes.service;

import com.mes.dto.request.ProcessStepRequest;
import com.mes.dto.response.ProcessStepResponse;
import com.mes.entity.ProcessStep;
import com.mes.exception.MesException;
import com.mes.repository.ProcessStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessStepService {

    @Autowired
    private ProcessStepRepository processStepRepository;

    public List<ProcessStepResponse> getAllSteps() {
        return processStepRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProcessStepResponse> getActiveSteps() {
        return processStepRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProcessStepResponse getStepById(Integer id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProcessStepResponse createStep(ProcessStepRequest request) {
        if (processStepRepository.existsByStepCode(request.getStepCode())) {
            throw new MesException("Mã công đoạn '" + request.getStepCode() + "' đã tồn tại");
        }
        ProcessStep step = new ProcessStep();
        step.setStepCode(request.getStepCode());
        step.setStepName(request.getStepName());
        step.setDescription(request.getDescription());
        step.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return toResponse(processStepRepository.save(step));
    }

    @Transactional
    public ProcessStepResponse updateStep(Integer id, ProcessStepRequest request) {
        ProcessStep step = findById(id);
        if (!step.getStepCode().equals(request.getStepCode())
                && processStepRepository.existsByStepCode(request.getStepCode())) {
            throw new MesException("Mã công đoạn '" + request.getStepCode() + "' đã tồn tại");
        }
        step.setStepCode(request.getStepCode());
        step.setStepName(request.getStepName());
        step.setDescription(request.getDescription());
        if (request.getIsActive() != null) step.setIsActive(request.getIsActive());
        return toResponse(processStepRepository.save(step));
    }

    @Transactional
    public void deleteStep(Integer id) {
        ProcessStep step = findById(id);
        processStepRepository.delete(step);
    }

    // --- Helper ---
    public ProcessStep findById(Integer id) {
        return processStepRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy công đoạn với ID: " + id, HttpStatus.NOT_FOUND));
    }

    private ProcessStepResponse toResponse(ProcessStep s) {
        ProcessStepResponse res = new ProcessStepResponse();
        res.setId(s.getId());
        res.setStepCode(s.getStepCode());
        res.setStepName(s.getStepName());
        res.setDescription(s.getDescription());
        res.setIsActive(s.getIsActive());
        res.setCreatedAt(s.getCreatedAt());
        return res;
    }
}
