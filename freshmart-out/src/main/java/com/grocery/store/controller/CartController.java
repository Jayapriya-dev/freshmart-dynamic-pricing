package com.grocery.store.controller;

import com.grocery.store.dto.request.CartRequest;
import com.grocery.store.dto.response.ApiResponse;
import com.grocery.store.dto.response.CartItemResponse;
import com.grocery.store.entity.User;
import com.grocery.store.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart
     * View current user's cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully",
                cartService.getCart(currentUser.getId())));
    }

    /**
     * POST /api/cart
     * Add a product to cart
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product added to cart",
                cartService.addToCart(currentUser.getId(), request)));
    }

    /**
     * PUT /api/cart/{cartItemId}
     * Update quantity of a cart item
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long cartItemId,
            @RequestParam @Min(value = 0, message = "Quantity must be 0 or more") Integer quantity) {

        CartItemResponse updated = cartService.updateCartItem(
                currentUser.getId(), cartItemId, quantity);

        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart (quantity set to 0)"));
        }
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", updated));
    }

    /**
     * DELETE /api/cart/{cartItemId}
     * Remove a single item from cart
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(currentUser.getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    /**
     * DELETE /api/cart
     * Clear the entire cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal User currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
