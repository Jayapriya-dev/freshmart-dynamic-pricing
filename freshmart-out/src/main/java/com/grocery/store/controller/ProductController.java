package com.grocery.store.controller;

import com.grocery.store.dto.response.ApiResponse;
import com.grocery.store.dto.response.ProductResponse;
import com.grocery.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Get all available products (ADMIN sees all, CUSTOMER sees available only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailableProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                "Products retrieved successfully",
                productService.getAvailableProducts()));
    }

    /**
     * GET /api/products/{id}
     * Get a single product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product retrieved successfully",
                productService.getProductById(id)));
    }

    /**
     * GET /api/products/category/{category}
     * Filter available products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                "Products in category '" + category + "' retrieved",
                productService.getProductsByCategory(category)));
    }

    /**
     * GET /api/products/search?keyword=
     * Search products by name or category
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(
                "Search results for: " + keyword,
                productService.searchProducts(keyword)));
    }
}
