package com.mes.service;

import com.mes.dto.request.ProductionOrderRequest;
import com.mes.dto.response.ProductionOrderResponse;
import com.mes.dto.response.ProductSerialResponse;
import com.mes.entity.*;
import com.mes.exception.MesException;
import com.mes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductionOrderService {

    @Autowired
    private ProductionOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductSerialRepository serialRepository;

    @Autowired
    private ProductProcessRouteRepository routeRepository;

    public List<ProductionOrderResponse> getAllOrders(String keyword, String status, Integer productId) {
        return orderRepository.searchOrders(keyword, status, productId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductionOrderResponse getOrderById(Integer id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProductionOrderResponse createOrder(ProductionOrderRequest request, String creatorUsername) {
        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            throw new MesException("Mã lệnh sản xuất '" + request.getOrderCode() + "' đã tồn tại");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new MesException("Không tìm thấy sản phẩm"));

        // Kiểm tra sản phẩm có quy trình chưa
        int routeCount = routeRepository.countByProductId(product.getId());
        if (routeCount == 0) {
            throw new MesException("Sản phẩm '" + product.getProductCode() + "' chưa có quy trình công đoạn. Vui lòng thiết lập trước.");
        }

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new MesException("Không tìm thấy người dùng"));

        ProductionOrder order = new ProductionOrder();
        order.setOrderCode(request.getOrderCode());
        order.setProduct(product);
        order.setPlannedQuantity(request.getPlannedQuantity());
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setStatus("CREATED");
        order.setCreatedBy(creator);
        order.setNotes(request.getNotes());

        order = orderRepository.save(order);

        // Tự động sinh serial sản phẩm
        generateSerials(order, product);

        return toResponse(order);
    }

    /**
     * Sinh danh sách serial theo format: ORDER_CODE-PRODUCT_CODE-0001
     */
    private void generateSerials(ProductionOrder order, Product product) {
        for (int i = 1; i <= order.getPlannedQuantity(); i++) {
            String serialCode = String.format("%s-%s-%04d",
                    order.getOrderCode(), product.getProductCode(), i);

            ProductSerial serial = new ProductSerial();
            serial.setSerialCode(serialCode);
            serial.setProductionOrder(order);
            serial.setProduct(product);
            serial.setStatus("WAITING");
            serialRepository.save(serial);
        }
    }

    // @Transactional
    // public ProductionOrderResponse updateOrderStatus(Integer id, String newStatus) {
    //     ProductionOrder order = findById(id);
    //     validateStatusTransition(order.getStatus(), newStatus);
    //     order.setStatus(newStatus);
    //     return toResponse(orderRepository.save(order));
    // }
    @Transactional
public ProductionOrderResponse updateOrderStatus(Integer id, String newStatus) {

    ProductionOrder order = findById(id);

    validateStatusTransition(order.getStatus(), newStatus);

    order.setStatus(newStatus);

    // THÊM ĐOẠN NÀY
    if (newStatus.equals("IN_PROGRESS")) {

        ProductProcessRoute firstRoute = routeRepository
                .findByProductIdAndStepOrder(
                        order.getProduct().getId(),
                        1
                )
                .orElseThrow(() ->
                        new MesException("Không tìm thấy công đoạn đầu tiên")
                );

        List<ProductSerial> serials =
                serialRepository.findByProductionOrderId(order.getId());

        for (ProductSerial serial : serials) {

            if (serial.getStatus().equals("WAITING")
                    && serial.getCurrentStep() == null) {

                serial.setCurrentStep(firstRoute.getProcessStep());

                serialRepository.save(serial);
            }
        }
    }

    return toResponse(orderRepository.save(order));
}

    @Transactional
    public ProductionOrderResponse updateOrder(Integer id, ProductionOrderRequest request) {
        ProductionOrder order = findById(id);
        if (!order.getStatus().equals("CREATED")) {
            throw new MesException("Chỉ có thể chỉnh sửa lệnh sản xuất ở trạng thái CREATED");
        }
        order.setNotes(request.getNotes());
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        return toResponse(orderRepository.save(order));
    }

    public List<ProductSerialResponse> getSerialsByOrder(Integer orderId) {
        findById(orderId); // validate
        return serialRepository.findByProductionOrderId(orderId)
                .stream().map(this::toSerialResponse).collect(Collectors.toList());
    }

    private ProductSerialResponse toSerialResponse(ProductSerial s) {
        ProductSerialResponse r = new ProductSerialResponse();
        r.setId(s.getId());
        r.setSerialCode(s.getSerialCode());
        r.setStatus(s.getStatus());
        r.setOrderCode(s.getProductionOrder().getOrderCode());
        r.setProductCode(s.getProduct().getProductCode());
        r.setProductName(s.getProduct().getProductName());
        r.setCurrentStepName(s.getCurrentStep() != null ? s.getCurrentStep().getStepName() : null);
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }

    /**
     * Kiểm tra và cập nhật trạng thái lệnh sản xuất dựa trên serial
     */
    @Transactional
    public void refreshOrderStatus(Integer orderId) {
        ProductionOrder order = findById(orderId);
        if (order.getStatus().equals("CANCELLED")) return;

        long total = serialRepository.countByProductionOrderId(orderId);
        long finished = serialRepository.countByProductionOrderIdAndStatus(orderId, "FINISHED");
        long scrap = serialRepository.countByProductionOrderIdAndStatus(orderId, "SCRAP");
        long inProgress = serialRepository.countByProductionOrderIdAndStatus(orderId, "IN_PROGRESS");

        order.setCompletedQuantity((int) (finished + scrap));

        if (finished + scrap == total) {
            order.setStatus("COMPLETED");
        } else if (inProgress > 0 || finished > 0) {
            order.setStatus("IN_PROGRESS");
        }

        orderRepository.save(order);
    }

    private void validateStatusTransition(String current, String next) {
        boolean valid = false;
        switch (current) {
            case "CREATED":
                valid = next.equals("IN_PROGRESS") || next.equals("CANCELLED");
                break;
            case "IN_PROGRESS":
                valid = next.equals("COMPLETED") || next.equals("CANCELLED");
                break;
            default:
                break;
        }
        if (!valid) {
            throw new MesException("Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
    }

    public ProductionOrder findById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy lệnh sản xuất với ID: " + id, HttpStatus.NOT_FOUND));
    }

    private ProductionOrderResponse toResponse(ProductionOrder o) {
        ProductionOrderResponse res = new ProductionOrderResponse();
        res.setId(o.getId());
        res.setOrderCode(o.getOrderCode());
        res.setProductId(o.getProduct().getId());
        res.setProductCode(o.getProduct().getProductCode());
        res.setProductName(o.getProduct().getProductName());
        res.setPlannedQuantity(o.getPlannedQuantity());
        res.setCompletedQuantity(o.getCompletedQuantity());
        res.setStartDate(o.getStartDate());
        res.setEndDate(o.getEndDate());
        res.setStatus(o.getStatus());
        res.setCreatedByName(o.getCreatedBy().getFullName());
        res.setCreatedAt(o.getCreatedAt());
        res.setNotes(o.getNotes());

        // Thống kê nhanh serial
        List<Object[]> stats = serialRepository.countByOrderGroupedByStatus(o.getId());
        Map<String, Long> statusMap = stats.stream()
                .collect(Collectors.toMap(s -> (String) s[0], s -> (Long) s[1]));
        res.setSerialWaiting(statusMap.getOrDefault("WAITING", 0L));
        res.setSerialInProgress(statusMap.getOrDefault("IN_PROGRESS", 0L));
        res.setSerialFinished(statusMap.getOrDefault("FINISHED", 0L));
        res.setSerialNg(statusMap.getOrDefault("NG", 0L));
        res.setSerialScrap(statusMap.getOrDefault("SCRAP", 0L));

        return res;
    }
}
