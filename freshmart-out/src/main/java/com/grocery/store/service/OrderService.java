package com.grocery.store.service;

import com.grocery.store.dto.request.OrderRequest;
import com.grocery.store.dto.response.OrderResponse;
import com.grocery.store.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(Long userId, OrderRequest request);

    List<OrderResponse> getOrderHistory(Long userId);

    OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}
