package com.mes.service;

import com.mes.dto.request.ExecutionRequest;
import com.mes.dto.response.ScanResponse;
import com.mes.entity.*;
import com.mes.exception.MesException;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý quét QR/Barcode và thực thi công đoạn sản xuất
 */
@Service
public class ScanService {

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProductProcessRouteRepository routeRepository;

    @Autowired
    private ProductionHistoryRepository historyRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private DefectLogRepository defectLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductionOrderService orderService;

    private static final List<String> VALID_RESULTS = Arrays.asList("OK", "NG", "REWORK", "SCRAP", "HOLD");

    /**
     * Lấy thông tin serial khi quét QR/Barcode
     */
    public ScanResponse getSerialInfo(String serialCode) {
        ProductSerial serial = findSerial(serialCode);
        return buildScanResponse(serial);
    }

    /**
     * Xử lý thực thi công đoạn sản xuất
     * Logic nghiệp vụ chính: kiểm soát luồng công đoạn
     */
    @Transactional
    public ScanResponse executeStep(ExecutionRequest request, String operatorUsername) {
        // Validate đầu vào
        if (!VALID_RESULTS.contains(request.getResult())) {
            throw new MesException("Kết quả không hợp lệ. Các giá trị hợp lệ: OK, NG, REWORK, SCRAP, HOLD");
        }

        ProductSerial serial = findSerial(request.getSerialCode());

        // Không cho cập nhật serial đã SCRAP hoặc FINISHED
        if ("SCRAP".equals(serial.getStatus())) {
            throw new MesException("Sản phẩm '" + serial.getSerialCode() + "' đã bị loại bỏ (SCRAP), không thể tiếp tục");
        }
        if ("FINISHED".equals(serial.getStatus())) {
            throw new MesException("Sản phẩm '" + serial.getSerialCode() + "' đã hoàn thành toàn bộ quy trình");
        }

        User operator = userRepository.findByUsername(operatorUsername)
                .orElseThrow(() -> new MesException("Không tìm thấy người dùng"));

        Integer productId = serial.getProduct().getId();
        Integer stepId = request.getStepId();

        // Kiểm tra sản phẩm có đang ở đúng công đoạn hiện tại không
        if (serial.getCurrentStep() != null &&
            !serial.getCurrentStep().getId().equals(stepId)) {

            throw new MesException(
                "Sản phẩm hiện đang ở công đoạn: "
                + serial.getCurrentStep().getStepName()
            );
        }

        // Lấy route hiện tại
        ProductProcessRoute currentRoute = routeRepository
                .findByProductIdAndProcessStepId(productId, stepId)
                .orElseThrow(() -> new MesException("Công đoạn này không nằm trong quy trình của sản phẩm"));

        // Kiểm tra thứ tự công đoạn - không được thực hiện công đoạn trước khi hoàn thành công đoạn trước
        validateStepOrder(serial, currentRoute, productId);

        // Tạo bản ghi lịch sử
        ProductionHistory history = new ProductionHistory();
        history.setProductSerial(serial);
        history.setProcessStep(currentRoute.getProcessStep());
        history.setOperator(operator);
        history.setStartTime(LocalDateTime.now());
        history.setEndTime(LocalDateTime.now());
        history.setResult(request.getResult());
        history.setNotes(request.getNotes());
        historyRepository.save(history);

        // Cập nhật trạng thái serial theo kết quả
        switch (request.getResult()) {
            case "OK":
                handleOK(serial, currentRoute, productId);
                break;
            case "NG":
                serial.setStatus("NG");
                serial.setCurrentStep(currentRoute.getProcessStep());
                // Ghi nhận lỗi nếu có defectId
                if (request.getDefectId() != null) {
                    logDefect(serial, request.getDefectId(), stepId, operator, request.getNotes());
                }
                break;
            case "REWORK":
                // Rework: quay về công đoạn cần làm lại
                handleRework(serial, request, productId);
                break;
            case "SCRAP":
                serial.setStatus("SCRAP");
                serial.setCurrentStep(currentRoute.getProcessStep());
                break;
            case "HOLD":
                serial.setStatus("HOLD");
                serial.setCurrentStep(currentRoute.getProcessStep());
                break;
        }

        serialRepository.save(serial);

        // Cập nhật trạng thái lệnh sản xuất
        orderService.refreshOrderStatus(serial.getProductionOrder().getId());

        return buildScanResponse(serial);
    }

    /**
     * Xử lý khi kết quả OK:
     * - Nếu là công đoạn cuối → FINISHED
     * - Nếu chưa phải cuối → chuyển sang công đoạn tiếp theo
     */
    private void handleOK(ProductSerial serial, ProductProcessRoute currentRoute, Integer productId) {
        int maxOrder = routeRepository.findMaxStepOrderByProductId(productId).orElse(0);

        if (currentRoute.getStepOrder() >= maxOrder) {
            // Hoàn thành toàn bộ quy trình
            serial.setStatus("FINISHED");
            serial.setCurrentStep(null);
        } else {
            // Chuyển sang công đoạn tiếp theo
            int nextOrder = currentRoute.getStepOrder() + 1;
            Optional<ProductProcessRoute> nextRoute =
                    routeRepository.findByProductIdAndStepOrder(productId, nextOrder);
            if (nextRoute.isPresent()) {
                serial.setStatus("IN_PROGRESS");
                serial.setCurrentStep(nextRoute.get().getProcessStep());
            } else {
                serial.setStatus("FINISHED");
                serial.setCurrentStep(null);
            }
        }
    }

    /**
     * Xử lý REWORK: quay lại công đoạn được chỉ định
     */
    // private void handleRework(ProductSerial serial, ExecutionRequest request, Integer productId) {
    //     serial.setStatus("REWORK");
    //     if (request.getReworkStepId() != null) {
    //         ProcessStep reworkStep = routeRepository
    //                 .findByProductIdAndProcessStepId(productId, request.getReworkStepId())
    //                 .map(r -> r.getProcessStep())
    //                 .orElseThrow(() -> new MesException("Công đoạn rework không hợp lệ"));
    //         serial.setCurrentStep(reworkStep);
    //     }
    // }
    /**
 * Xử lý REWORK: quay lại công đoạn được chỉ định
 */
            private void handleRework(ProductSerial serial,
                                    ExecutionRequest request,
                                    Integer productId) {

                serial.setStatus("REWORK");

                if (request.getReworkStepId() == null) {
                    throw new MesException(
                            "Vui lòng chọn công đoạn làm lại");
                }

                // Công đoạn hiện tại
                ProductProcessRoute currentRoute =
                        routeRepository
                                .findByProductIdAndProcessStepId(
                                        productId,
                                        serial.getCurrentStep().getId())
                                .orElseThrow(() ->
                                        new MesException(
                                                "Không tìm thấy công đoạn hiện tại"));

                // Công đoạn muốn rework
                ProductProcessRoute reworkRoute =
                        routeRepository
                                .findByProductIdAndProcessStepId(
                                        productId,
                                        request.getReworkStepId())
                                .orElseThrow(() ->
                                        new MesException(
                                                "Công đoạn rework không hợp lệ"));

                // Không cho rework tới công đoạn phía sau
                if (reworkRoute.getStepOrder()
                        > currentRoute.getStepOrder()) {

                    throw new MesException(
                            "Không thể rework tới công đoạn phía sau");
                }

                serial.setCurrentStep(
                        reworkRoute.getProcessStep());
            }

    /**
     * Kiểm tra thứ tự công đoạn: phải theo thứ tự, không được nhảy công đoạn
     */
    private void validateStepOrder(ProductSerial serial, ProductProcessRoute currentRoute, Integer productId) {
        int currentStepOrder = currentRoute.getStepOrder();

        // Nếu đây là công đoạn đầu tiên (order = 1), luôn cho phép
        if (currentStepOrder == 1) return;

        // Kiểm tra công đoạn trước đó đã hoàn thành chưa
        Optional<ProductProcessRoute> prevRoute =
                routeRepository.findByProductIdAndStepOrder(productId, currentStepOrder - 1);

        if (prevRoute.isPresent()) {
            boolean prevCompleted =
                historyRepository.hasCompletedStep(
                serial.getId(),
                prevRoute.get().getProcessStep().getId()
        ) > 0;
            //boolean prevCompleted = historyRepository.hasCompletedStep(
                    //serial.getId(), prevRoute.get().getProcessStep().getId());

            if (!prevCompleted) {
                throw new MesException("Công đoạn '"
                        + prevRoute.get().getProcessStep().getStepName()
                        + "' (thứ tự " + (currentStepOrder - 1)
                        + ") chưa được hoàn thành. Không thể thực hiện công đoạn này.");
            }
        }
    }

    /**
     * Ghi nhận lỗi chất lượng
     */
    private void logDefect(ProductSerial serial, Integer defectId, Integer stepId,
                            User reporter, String notes) {
        Defect defect = defectRepository.findById(defectId)
                .orElseThrow(() -> new MesException("Không tìm thấy loại lỗi"));

        ProcessStep step = serial.getCurrentStep();
        if (step == null) {
            routeRepository.findByProductIdAndProcessStepId(serial.getProduct().getId(), stepId)
                    .ifPresent(r -> {
                        DefectLog log = new DefectLog();
                        log.setProductSerial(serial);
                        log.setDefect(defect);
                        log.setProcessStep(r.getProcessStep());
                        log.setReportedBy(reporter);
                        log.setNotes(notes);
                        defectLogRepository.save(log);
                    });
        } else {
            DefectLog log = new DefectLog();
            log.setProductSerial(serial);
            log.setDefect(defect);
            log.setProcessStep(step);
            log.setReportedBy(reporter);
            log.setNotes(notes);
            defectLogRepository.save(log);
        }
    }

    /**
     * Build response chi tiết cho serial sau khi quét
     */
    private ScanResponse buildScanResponse(ProductSerial serial) {
        Integer productId = serial.getProduct().getId();
        List<ProductProcessRoute> routes = routeRepository.findByProductIdOrderByStepOrderAsc(productId);
        int totalSteps = routes.size();

        ScanResponse response = new ScanResponse();
        response.setSerialId(serial.getId());
        response.setSerialCode(serial.getSerialCode());
        response.setStatus(serial.getStatus());
        response.setOrderCode(serial.getProductionOrder().getOrderCode());
        response.setOrderId(serial.getProductionOrder().getId());
        response.setProductCode(serial.getProduct().getProductCode());
        response.setProductName(serial.getProduct().getProductName());
        response.setTotalSteps(totalSteps);
        response.setUpdatedAt(serial.getUpdatedAt());

        //chon tram khi rework
        List<ScanResponse.StepItem> stepItems =
                    routes.stream()
                            .map(r -> {
                                ScanResponse.StepItem item =
                                        new ScanResponse.StepItem();

                                item.setId(
                                        r.getProcessStep().getId()
                                );

                                item.setStepCode(
                                        r.getProcessStep().getStepCode()
                                );

                                item.setStepName(
                                        r.getProcessStep().getStepName()
                                );

                                item.setStepOrder(
                                        r.getStepOrder()
                                );

                                return item;
                            })
                            .collect(Collectors.toList());

            response.setProcessSteps(stepItems);

        // Công đoạn hiện tại
        if (serial.getCurrentStep() != null) {
            response.setCurrentStepId(serial.getCurrentStep().getId());
            response.setCurrentStepCode(serial.getCurrentStep().getStepCode());
            response.setCurrentStepName(serial.getCurrentStep().getStepName());

            // Tìm thứ tự công đoạn hiện tại
            routes.stream()
                    .filter(r -> r.getProcessStep().getId().equals(serial.getCurrentStep().getId()))
                    .findFirst()
                    .ifPresent(r -> {
                        response.setCurrentStepOrder(r.getStepOrder());
                        // Tìm công đoạn tiếp theo
                        routes.stream()
                                .filter(nr -> nr.getStepOrder() == r.getStepOrder() + 1)
                                .findFirst()
                                .ifPresent(nr -> {
                                    response.setNextStepId(nr.getProcessStep().getId());
                                    response.setNextStepCode(nr.getProcessStep().getStepCode());
                                    response.setNextStepName(nr.getProcessStep().getStepName());
                                });
                    });
        } else if ("WAITING".equals(serial.getStatus()) && !routes.isEmpty()) {
            // Chưa bắt đầu → công đoạn đầu tiên
            ProductProcessRoute firstRoute = routes.get(0);
            response.setCurrentStepId(firstRoute.getProcessStep().getId());
            response.setCurrentStepCode(firstRoute.getProcessStep().getStepCode());
            response.setCurrentStepName(firstRoute.getProcessStep().getStepName());
            response.setCurrentStepOrder(firstRoute.getStepOrder());
            if (routes.size() > 1) {
                response.setNextStepId(routes.get(1).getProcessStep().getId());
                response.setNextStepCode(routes.get(1).getProcessStep().getStepCode());
                response.setNextStepName(routes.get(1).getProcessStep().getStepName());
            }
        }

        // Lịch sử
        List<ProductionHistory> histories = historyRepository
                .findByProductSerialIdOrderByCreatedAtDesc(serial.getId());
        List<ScanResponse.HistoryItem> historyItems = histories.stream().map(h -> {
            ScanResponse.HistoryItem item = new ScanResponse.HistoryItem();
            item.setId(h.getId());
            item.setStepCode(h.getProcessStep().getStepCode());
            item.setStepName(h.getProcessStep().getStepName());
            item.setResult(h.getResult());
            item.setOperatorName(h.getOperator().getFullName());
            item.setStartTime(h.getStartTime());
            item.setEndTime(h.getEndTime());
            item.setNotes(h.getNotes());
            return item;
        }).collect(Collectors.toList());
        response.setHistory(historyItems);

        return response;
    }

    private ProductSerial findSerial(String serialCode) {
        return serialRepository.findBySerialCode(serialCode)
                .orElseThrow(() -> new MesException("Không tìm thấy sản phẩm với mã: " + serialCode));
    }
}
