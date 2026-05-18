package com.mes.service;

import com.mes.dto.request.ProductRequest;
import com.mes.dto.request.RouteRequest;
import com.mes.dto.response.ProductResponse;
import com.mes.dto.response.RouteResponse;
import com.mes.entity.ProcessStep;
import com.mes.entity.Product;
import com.mes.entity.ProductProcessRoute;
import com.mes.exception.MesException;
import com.mes.repository.ProcessStepRepository;
import com.mes.repository.ProductProcessRouteRepository;
import com.mes.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProcessStepRepository processStepRepository;

    @Autowired
    private ProductProcessRouteRepository routeRepository;

    // ===== SẢN PHẨM =====

    public List<ProductResponse> getAllProducts(String keyword) {
        List<Product> products = (keyword != null && !keyword.trim().isEmpty())
                ? productRepository.searchProducts(keyword.trim())
                : productRepository.findAll();
        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(Integer id) {
        return toResponse(findProductById(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new MesException("Mã sản phẩm '" + request.getProductCode() + "' đã tồn tại");
        }
        Product product = new Product();
        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setComponentType(request.getComponentType());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = findProductById(id);
        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new MesException("Mã sản phẩm '" + request.getProductCode() + "' đã tồn tại");
        }
        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setComponentType(request.getComponentType());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus() != null ? request.getStatus() : product.getStatus());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Integer id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    // ===== QUY TRÌNH CÔNG ĐOẠN =====

    public List<RouteResponse> getRoutesByProduct(Integer productId) {
        findProductById(productId); // validate exists
        return routeRepository.findByProductIdOrderByStepOrderAsc(productId)
                .stream().map(this::toRouteResponse).collect(Collectors.toList());
    }

    @Transactional
    public RouteResponse addRouteStep(Integer productId, RouteRequest request) {
        Product product = findProductById(productId);
        ProcessStep step = processStepRepository.findById(request.getProcessStepId())
                .orElseThrow(() -> new MesException("Không tìm thấy công đoạn"));

        if (routeRepository.existsByProductIdAndStepOrder(productId, request.getStepOrder())) {
            throw new MesException("Thứ tự " + request.getStepOrder() + " đã tồn tại trong quy trình này");
        }

        ProductProcessRoute route = new ProductProcessRoute();
        route.setProduct(product);
        route.setProcessStep(step);
        route.setStepOrder(request.getStepOrder());
        route.setIsMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : true);

        return toRouteResponse(routeRepository.save(route));
    }

    @Transactional
    public void deleteRouteStep(Integer routeId) {
        ProductProcessRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new MesException("Không tìm thấy bước quy trình", HttpStatus.NOT_FOUND));
        routeRepository.delete(route);
    }

    @Transactional
    public List<RouteResponse> saveFullRoute(Integer productId, List<RouteRequest> requests) {
        findProductById(productId); // validate
        routeRepository.deleteByProductId(productId);
        routeRepository.flush();

        for (RouteRequest req : requests) {
            addRouteStep(productId, req);
        }
        return getRoutesByProduct(productId);
    }

    // --- Helpers ---
    public Product findProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy sản phẩm với ID: " + id, HttpStatus.NOT_FOUND));
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse res = new ProductResponse();
        res.setId(p.getId());
        res.setProductCode(p.getProductCode());
        res.setProductName(p.getProductName());
        res.setComponentType(p.getComponentType());
        res.setDescription(p.getDescription());
        res.setStatus(p.getStatus());
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }

    private RouteResponse toRouteResponse(ProductProcessRoute r) {
        RouteResponse res = new RouteResponse();
        res.setId(r.getId());
        res.setProductId(r.getProduct().getId());
        res.setProcessStepId(r.getProcessStep().getId());
        res.setStepCode(r.getProcessStep().getStepCode());
        res.setStepName(r.getProcessStep().getStepName());
        res.setStepOrder(r.getStepOrder());
        res.setIsMandatory(r.getIsMandatory());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
