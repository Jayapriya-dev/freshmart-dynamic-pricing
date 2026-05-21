package com.grocery.store.service.impl;

import com.grocery.store.dto.request.OrderRequest;
import com.grocery.store.dto.response.OrderItemResponse;
import com.grocery.store.dto.response.OrderResponse;
import com.grocery.store.entity.*;
import com.grocery.store.exception.BadRequestException;
import com.grocery.store.exception.InsufficientStockException;
import com.grocery.store.exception.ResourceNotFoundException;
import com.grocery.store.exception.UnauthorizedException;
import com.grocery.store.repository.CartItemRepository;
import com.grocery.store.repository.OrderRepository;
import com.grocery.store.repository.ProductRepository;
import com.grocery.store.repository.UserRepository;
import com.grocery.store.service.OrderService;
import com.grocery.store.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = findUserById(userId);
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add products before placing an order.");
        }

        // Validate stock and lock in prices
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Validate availability
            if (!product.isAvailable()) {
                throw new BadRequestException(
                        "Product '" + product.getName() + "' is no longer available.");
            }

            // Validate stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), cartItem.getQuantity(), product.getStockQuantity());
            }

            // Calculate final price at time of order
            BigDecimal unitPrice = pricingService.calculateFinalPrice(product);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.addOrderItem(orderItem);
            total = total.add(subtotal);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setAvailable(false);
            }
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order
        cartItemRepository.deleteByUserId(userId);

        log.info("Order placed: id={} for user={}, total=₹{}", savedOrder.getId(), userId, total);
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdWithItems(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to view this order.");
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllWithItems()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Business rule: cannot revert a delivered or cancelled order
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot change status of an order that is already " + order.getStatus());
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, status);
        return mapToResponse(updated);
    }

    // ==================== Mapper ====================

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .category(item.getProduct().getCategory())
                        .imageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .orderItems(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
