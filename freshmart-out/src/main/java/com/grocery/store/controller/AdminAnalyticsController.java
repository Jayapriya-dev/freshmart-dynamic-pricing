package com.grocery.store.controller;

import com.grocery.store.dto.response.AnalyticsSummary;
import com.grocery.store.dto.response.ApiResponse;
import com.grocery.store.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminAnalyticsController — REST Controller for Admin Dashboard APIs.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Interview explanation for this class:                                  │
 * │                                                                         │
 * │  "This controller exposes a single endpoint: GET /api/admin/analytics.  │
 * │   It's secured with @PreAuthorize so only ADMIN users can call it.      │
 * │   It delegates all logic to AnalyticsService (clean separation).        │
 * │   It returns the result wrapped in ApiResponse — our standard envelope  │
 * │   that every API in FreshMart uses for consistency."                    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Annotations explained:
 *
 *  @RestController    — Combines @Controller + @ResponseBody.
 *                       Every method returns JSON automatically (no @ResponseBody needed per method).
 *
 *  @RequestMapping    — All routes in this class start with /api/admin/analytics
 *
 *  @RequiredArgsConstructor — Lombok injects AnalyticsService via constructor.
 *
 *  @PreAuthorize      — Spring Security: only users with ROLE_ADMIN can reach ANY
 *                       endpoint in this class.  If a non-admin calls it, Spring
 *                       automatically returns HTTP 403 Forbidden.
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/admin/analytics/summary
     *
     * Returns all dashboard numbers in one API call:
     *  - total products
     *  - total orders
     *  - total revenue
     *  - top product (most ordered)
     *  - chart data (product names, order counts, prices)
     *
     * Why ONE endpoint for everything?
     *   → The frontend makes a single fetch() call and gets all data at once.
     *   → Fewer network round-trips = faster page load.
     *   → For a junior project this is perfectly clean. In a big production app
     *     you might split these into separate endpoints.
     *
     * HTTP Response example:
     * {
     *   "success": true,
     *   "message": "Analytics data fetched successfully",
     *   "data": {
     *     "totalProducts": 12,
     *     "totalOrders": 47,
     *     "totalRevenue": 18450.75,
     *     "topProductName": "Organic Apples",
     *     "topProductOrderCount": 120,
     *     "productNames": ["Apples", "Milk", ...],
     *     "productOrderCounts": [120, 85, ...],
     *     "productBasePrices": [49.99, 35.00, ...]
     *   }
     * }
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AnalyticsSummary>> getAnalyticsSummary() {
        AnalyticsSummary summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(
                ApiResponse.success("Analytics data fetched successfully", summary)
        );
    }
}

