package com.grocery.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private Integer stockQuantity;
    private Double discountPercentage;
    private boolean available;
    private String imageUrl;
    private String priceBreakdown;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
