package com.grocery.store.service;

import com.grocery.store.dto.request.ProductRequest;
import com.grocery.store.dto.request.UpdateStockRequest;
import com.grocery.store.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);

    ProductResponse getProductById(Long productId);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getAvailableProducts();

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> searchProducts(String keyword);

    ProductResponse updateStock(Long productId, UpdateStockRequest request);

    ProductResponse updateDiscount(Long productId, Double discountPercentage);
}
