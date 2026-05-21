package com.grocery.store.repository;

import com.grocery.store.entity.Order;
import com.grocery.store.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderRepository — database access layer for Order.
 *
 * ──────────────────────────────────────────────────────────────────────────
 * ANALYTICS ADDITIONS (new methods added at the bottom of this file)
 * ──────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ── Existing queries (do NOT change these) ───────────────────────────────

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdOrderByOrderedAtDesc(Long userId);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product")
    List<Order> findAllWithItems();

    // ── NEW: Analytics queries ────────────────────────────────────────────────

    /**
     * Calculate total revenue — sum of totalAmount across all orders.
     *
     * JPQL explanation:
     *   SELECT SUM(o.totalAmount)  ← add up the totalAmount column
     *   FROM Order o               ← from the orders table
     *
     * Return type is BigDecimal because money must be precise.
     * The query can return NULL if there are no orders at all,
     * so we handle that in the service with a null-check.
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal calculateTotalRevenue();

    /**
     * Count orders (all statuses).
     *
     * Note: JpaRepository already has count() which does exactly this.
     * We call repository.count() in the service.  No extra method needed here.
     */
}
