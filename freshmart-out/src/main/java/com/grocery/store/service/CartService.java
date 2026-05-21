package com.grocery.store.service;

import com.grocery.store.dto.request.CartRequest;
import com.grocery.store.dto.response.CartItemResponse;

import java.util.List;

public interface CartService {

    CartItemResponse addToCart(Long userId, CartRequest request);

    CartItemResponse updateCartItem(Long userId, Long cartItemId, Integer quantity);

    void removeCartItem(Long userId, Long cartItemId);

    List<CartItemResponse> getCart(Long userId);

    void clearCart(Long userId);
}
