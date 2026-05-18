package com.mes.controller;

import com.mes.dto.request.ProductionOrderRequest;
import com.mes.dto.response.ProductionOrderResponse;
import com.mes.dto.response.ProductSerialResponse;
import com.mes.service.ProductionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/production-orders")
public class ProductionOrderController {

    @Autowired
    private ProductionOrderService orderService;

    @GetMapping
    public ResponseEntity<List<ProductionOrderResponse>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer productId) {
        return ResponseEntity.ok(orderService.getAllOrders(keyword, status, productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionOrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProductionOrderResponse> createOrder(
            @Valid @RequestBody ProductionOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProductionOrderResponse> updateOrder(
            @PathVariable Integer id, @RequestBody ProductionOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProductionOrderResponse> updateStatus(
            @PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus));
    }

    @GetMapping("/{id}/serials")
    public ResponseEntity<List<ProductSerialResponse>> getSerials(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getSerialsByOrder(id));
    }
}
