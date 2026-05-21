package com.grocery.store.service.impl;

import com.grocery.store.dto.request.CartRequest;
import com.grocery.store.dto.response.CartItemResponse;
import com.grocery.store.entity.CartItem;
import com.grocery.store.entity.Product;
import com.grocery.store.entity.User;
import com.grocery.store.exception.BadRequestException;
import com.grocery.store.exception.ResourceNotFoundException;
import com.grocery.store.exception.UnauthorizedException;
import com.grocery.store.repository.CartItemRepository;
import com.grocery.store.repository.ProductRepository;
import com.grocery.store.repository.UserRepository;
import com.grocery.store.service.CartService;
import com.grocery.store.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;

    @Override
    @Transactional
    public CartItemResponse addToCart(Long userId, CartRequest request) {
        User user = findUserById(userId);
        Product product = findAvailableProduct(request.getProductId());

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException(
                    String.format("Requested quantity (%d) exceeds available stock (%d) for '%s'",
                            request.getQuantity(), product.getStockQuantity(), product.getName()));
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, product.getId());

        CartItem cartItem;
        if (existing.isPresent()) {
            // Update quantity if already in cart
            cartItem = existing.get();
            int newQty = cartItem.getQuantity() + request.getQuantity();
            if (newQty > product.getStockQuantity()) {
                throw new BadRequestException(
                        String.format("Total cart quantity (%d) exceeds stock (%d).",
                                newQty, product.getStockQuantity()));
            }
            cartItem.setQuantity(newQty);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem saved = cartItemRepository.save(cartItem);
        log.info("Cart updated for user {}: product {} x{}", userId, product.getName(), saved.getQuantity());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = findCartItemById(cartItemId);
        validateOwnership(cartItem, userId);

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Cart item removed (quantity=0) for user {}", userId);
            return null;
        }

        if (quantity > cartItem.getProduct().getStockQuantity()) {
            throw new BadRequestException(
                    String.format("Requested quantity (%d) exceeds stock (%d).",
                            quantity, cartItem.getProduct().getStockQuantity()));
        }

        cartItem.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(cartItem);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = findCartItemById(cartItemId);
        validateOwnership(cartItem, userId);
        cartItemRepository.delete(cartItem);
        log.info("Cart item {} removed for user {}", cartItemId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        log.info("Cart cleared for user {}", userId);
    }

    // ==================== Mapper ====================

    private CartItemResponse mapToResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal unitPrice = pricingService.calculateFinalPrice(product);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .quantity(cartItem.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .availableStock(product.getStockQuantity())
                .addedAt(cartItem.getAddedAt())
                .build();
    }

    // ==================== Helpers ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Product findAvailableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!product.isAvailable()) {
            throw new BadRequestException("Product '" + product.getName() + "' is not available.");
        }
        return product;
    }

    private CartItem findCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));
    }

    private void validateOwnership(CartItem cartItem, Long userId) {
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this cart item.");
        }
    }
}
