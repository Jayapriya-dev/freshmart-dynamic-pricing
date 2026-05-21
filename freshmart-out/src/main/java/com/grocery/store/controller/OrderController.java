package com.grocery.store.controller;

import com.grocery.store.dto.request.OrderRequest;
import com.grocery.store.dto.response.ApiResponse;
import com.grocery.store.dto.response.OrderResponse;
import com.grocery.store.entity.Role;
import com.grocery.store.entity.User;
import com.grocery.store.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Place an order from cart items (CUSTOMER only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.placeOrder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    /**
     * GET /api/orders
     * Get order history for logged-in customer
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved",
                orderService.getOrderHistory(currentUser.getId())));
    }

    /**
     * GET /api/orders/{id}
     * Get a specific order (customer sees own, admin sees any)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderById(id, currentUser.getId(), isAdmin)));
    }
}
