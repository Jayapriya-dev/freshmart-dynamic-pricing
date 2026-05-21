package com.grocery.store.service.impl;

import com.grocery.store.dto.request.ProductRequest;
import com.grocery.store.dto.request.UpdateStockRequest;
import com.grocery.store.dto.response.ProductResponse;
import com.grocery.store.entity.Product;
import com.grocery.store.exception.BadRequestException;
import com.grocery.store.exception.ResourceNotFoundException;
import com.grocery.store.repository.ProductRepository;
import com.grocery.store.service.PricingService;
import com.grocery.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PricingService pricingService;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .stockQuantity(request.getStockQuantity())
                .discountPercentage(request.getDiscountPercentage())
                .available(request.isAvailable())
                .imageUrl(request.getImageUrl())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {} (id={})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = findProductById(productId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBasePrice(request.getBasePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setDiscountPercentage(request.getDiscountPercentage());
        product.setAvailable(request.isAvailable());
        product.setImageUrl(request.getImageUrl());
        Product updated = productRepository.save(product);
        log.info("Product updated: {} (id={})", updated.getName(), updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);
        productRepository.delete(product);
        log.info("Product deleted: id={}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        return mapToResponse(findProductById(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByAvailableTrue()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByAvailableTrueAndCategoryIgnoreCase(category)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long productId, UpdateStockRequest request) {
        Product product = findProductById(productId);
        product.setStockQuantity(request.getStockQuantity());
        if (request.getStockQuantity() == 0) {
            product.setAvailable(false);
        }
        Product updated = productRepository.save(product);
        log.info("Stock updated for product id={}: new quantity={}", productId, request.getStockQuantity());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ProductResponse updateDiscount(Long productId, Double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new BadRequestException("Discount must be between 0 and 100.");
        }
        Product product = findProductById(productId);
        product.setDiscountPercentage(discountPercentage);
        Product updated = productRepository.save(product);
        log.info("Discount updated for product id={}: {}%", productId, discountPercentage);
        return mapToResponse(updated);
    }

    // ==================== Mapper ====================

    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .basePrice(product.getBasePrice())
                .finalPrice(pricingService.calculateFinalPrice(product))
                .stockQuantity(product.getStockQuantity())
                .discountPercentage(product.getDiscountPercentage())
                .available(product.isAvailable())
                .imageUrl(product.getImageUrl())
                .priceBreakdown(pricingService.getPriceBreakdown(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }
}
