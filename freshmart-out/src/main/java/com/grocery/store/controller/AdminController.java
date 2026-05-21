package com.grocery.store.controller;

import com.grocery.store.dto.request.ProductRequest;
import com.grocery.store.dto.request.UpdateStockRequest;
import com.grocery.store.dto.response.ApiResponse;
import com.grocery.store.dto.response.OrderResponse;
import com.grocery.store.dto.response.ProductResponse;
import com.grocery.store.entity.OrderStatus;
import com.grocery.store.service.OrderService;
import com.grocery.store.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;

    // ==================== Product Management ====================

    /**
     * POST /api/admin/products
     * Create a new product
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully",
                        productService.createProduct(request)));
    }

    /**
     * PUT /api/admin/products/{id}
     * Update an existing product
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.updateProduct(id, request)));
    }

    /**
     * DELETE /api/admin/products/{id}
     * Delete a product
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }

    /**
     * GET /api/admin/products
     * Get all products (including unavailable)
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success("All products retrieved",
                productService.getAllProducts()));
    }

    /**
     * PATCH /api/admin/products/{id}/stock
     * Update product stock quantity
     */
    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully",
                productService.updateStock(id, request)));
    }

    /**
     * PATCH /api/admin/products/{id}/discount
     * Set discount percentage for a product
     */
    @PatchMapping("/products/{id}/discount")
    public ResponseEntity<ApiResponse<ProductResponse>> updateDiscount(
            @PathVariable Long id,
            @RequestParam
            @DecimalMin(value = "0.0", message = "Discount cannot be negative")
            @DecimalMax(value = "100.0", message = "Discount cannot exceed 100")
            Double discountPercentage) {
        return ResponseEntity.ok(ApiResponse.success(
                "Discount updated to " + discountPercentage + "%",
                productService.updateDiscount(id, discountPercentage)));
    }

    // ==================== Order Management ====================

    /**
     * GET /api/admin/orders
     * View all customer orders
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success("All orders retrieved",
                orderService.getAllOrders()));
    }

    /**
     * GET /api/admin/orders/{id}
     * View a specific order
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderById(id, null, true)));
    }

    /**
     * PATCH /api/admin/orders/{id}/status
     * Update order status
     */
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated to " + status,
                orderService.updateOrderStatus(id, status)));
    }
}
