package com.grocery.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AnalyticsSummary — Data Transfer Object (DTO) for admin analytics.
 *
 * This class is a "data bag" — it holds all the numbers we want to
 * show on the admin dashboard.  When our API is called, Spring will
 * automatically convert this object into JSON and send it to the browser.
 *
 * Lombok annotations explanation:
 *   @Data          — generates getters, setters, equals, hashCode, toString
 *   @Builder       — lets us build objects like: AnalyticsSummary.builder().totalProducts(5).build()
 *   @NoArgsConstructor / @AllArgsConstructor — generate constructors automatically
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {

    // ── Overview counters ────────────────────────────────────────────────────

    /** Total number of products in the database (available + unavailable) */
    private long totalProducts;

    /** Total number of orders placed (all statuses) */
    private long totalOrders;

    /**
     * Sum of totalAmount across every order.
     * BigDecimal is used for money so we never lose precision with floating-point.
     */
    private BigDecimal totalRevenue;

    // ── Most popular product ─────────────────────────────────────────────────

    /** ID of the product that appears most in order items */
    private Long topProductId;

    /** Human-readable name of the most-ordered product */
    private String topProductName;

    /** How many times that product has been ordered */
    private long topProductOrderCount;

    // ── Chart data (lists that Chart.js will consume) ────────────────────────

    /**
     * Labels for the bar chart — one entry per product.
     * Example: ["Apples", "Milk", "Bread"]
     */
    private List<String> productNames;

    /**
     * Values for the bar chart — total quantity ordered per product.
     * Example: [120, 85, 60]
     */
    private List<Long> productOrderCounts;

    /**
     * Base prices for the line chart — one entry per product.
     * Shown alongside order counts to visualise price vs demand.
     */
    private List<BigDecimal> productBasePrices;
}
