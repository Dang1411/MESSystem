package com.mes.controller;

import com.mes.dto.request.ProductRequest;
import com.mes.dto.request.RouteRequest;
import com.mes.dto.response.ProductResponse;
import com.mes.dto.response.RouteResponse;
import com.mes.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.getAllProducts(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ROUTES =====

    @GetMapping("/{productId}/routes")
    public ResponseEntity<List<RouteResponse>> getRoutes(@PathVariable Integer productId) {
        return ResponseEntity.ok(productService.getRoutesByProduct(productId));
    }

    @PostMapping("/{productId}/routes")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<RouteResponse> addRoute(
            @PathVariable Integer productId, @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addRouteStep(productId, request));
    }

    @PutMapping("/{productId}/routes")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<RouteResponse>> saveFullRoute(
            @PathVariable Integer productId, @RequestBody List<RouteRequest> requests) {
        return ResponseEntity.ok(productService.saveFullRoute(productId, requests));
    }

    @DeleteMapping("/{productId}/routes/{routeId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Integer productId, @PathVariable Integer routeId) {
        productService.deleteRouteStep(routeId);
        return ResponseEntity.noContent().build();
    }
}
