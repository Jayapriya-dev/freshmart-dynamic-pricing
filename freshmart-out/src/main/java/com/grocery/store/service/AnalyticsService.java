package com.grocery.store.service;

import com.grocery.store.dto.response.AnalyticsSummary;
import com.grocery.store.entity.Product;
import com.grocery.store.repository.OrderRepository;
import com.grocery.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * AnalyticsService — Business Logic Layer for Admin Analytics.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Architecture Reminder (great for interviews!)                          │
 * │                                                                         │
 * │  Browser → Controller → Service → Repository → Database                │
 * │                                                                         │
 * │  Controller:  receives HTTP requests, calls service, returns JSON       │
 * │  Service:     contains the actual LOGIC (this file)                     │
 * │  Repository:  talks to the database (runs queries)                      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Why keep logic in a Service and not in the Controller?
 *   → Clean code principle: each class has ONE job (Single Responsibility).
 *   → Controllers just route requests; services do the thinking.
 *   → Services can be unit-tested independently of HTTP.
 *   → If you later add a scheduled job or another controller that needs
 *     analytics, you just inject this service — no code duplication.
 *
 * @Service  — tells Spring "create one instance of this class and manage it"
 * @RequiredArgsConstructor — Lombok generates a constructor that injects
 *                            the two final repositories automatically.
 * @Slf4j    — gives us a `log` object for logging (great for debugging).
 * @Transactional(readOnly=true) — tells the database "I won't modify data,
 *                                  just read it" — allows DB optimisations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    /**
     * These are injected automatically by Spring (constructor injection).
     * The `final` keyword forces them to be provided at construction time.
     */
    private final ProductRepository productRepository;
    private final OrderRepository   orderRepository;

    /**
     * getAnalyticsSummary() — Main method: assembles the full dashboard data.
     *
     * It calls the repositories to get raw numbers, then packages everything
     * into an AnalyticsSummary DTO (Data Transfer Object) that the controller
     * will convert to JSON for the frontend.
     *
     * @return AnalyticsSummary — all numbers the dashboard needs
     */
    public AnalyticsSummary getAnalyticsSummary() {
        log.info("Building analytics summary...");

        // ── Step 1: Simple counts ────────────────────────────────────────────

        // count() is provided for free by JpaRepository — no @Query needed
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();

        log.debug("totalProducts={}, totalOrders={}", totalProducts, totalOrders);

        // ── Step 2: Total revenue ─────────────────────────────────────────────

        // Our custom @Query in OrderRepository sums all totalAmount values.
        // If there are 0 orders, SQL SUM returns NULL — we default to 0.
        BigDecimal rawRevenue = orderRepository.calculateTotalRevenue();
        BigDecimal totalRevenue = (rawRevenue != null) ? rawRevenue : BigDecimal.ZERO;

        log.debug("totalRevenue={}", totalRevenue);

        // ── Step 3: Product demand data for charts ────────────────────────────

        // Our custom @Query returns a List of Object[] arrays.
        // Each Object[] is: [ Product entity , Long totalQtyOrdered ]
        List<Object[]> demandRows = productRepository.findProductsSortedByDemand();

        // Prepare three parallel lists — Chart.js needs labels and values
        // as separate arrays.
        List<String>     productNames       = new ArrayList<>();
        List<Long>       productOrderCounts = new ArrayList<>();
        List<BigDecimal> productBasePrices  = new ArrayList<>();

        // Defaults for top product (in case there are no orders yet)
        Long   topProductId         = null;
        String topProductName       = "No orders yet";
        long   topProductOrderCount = 0;

        for (int i = 0; i < demandRows.size(); i++) {
            Object[] row     = demandRows.get(i);
            Product  product = (Product) row[0];   // index 0 = Product entity
            Long     count   = (Long)    row[1];   // index 1 = total qty ordered

            productNames.add(product.getName());
            productOrderCounts.add(count);
            productBasePrices.add(product.getBasePrice());

            // The first row is the top product (query ordered DESC)
            if (i == 0) {
                topProductId         = product.getId();
                topProductName       = product.getName();
                topProductOrderCount = count;
            }
        }

        log.info("Analytics summary built successfully. Top product: '{}'", topProductName);

        // ── Step 4: Build and return the DTO ─────────────────────────────────

        // Builder pattern (enabled by @Builder in AnalyticsSummary) lets us
        // set only the fields we want, in any order, very readably.
        return AnalyticsSummary.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .topProductId(topProductId)
                .topProductName(topProductName)
                .topProductOrderCount(topProductOrderCount)
                .productNames(productNames)
                .productOrderCounts(productOrderCounts)
                .productBasePrices(productBasePrices)
                .build();
    }
}

