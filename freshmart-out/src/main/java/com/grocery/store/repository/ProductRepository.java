package com.grocery.store.repository;

import com.grocery.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository — database access layer for Product.
 *
 * Spring Data JPA "magic":
 *   → Extending JpaRepository<Product, Long> gives us save(), findById(),
 *     findAll(), count(), deleteById() etc. for FREE — no SQL needed.
 *
 *   → Methods like findByAvailableTrue() are auto-implemented by Spring
 *     by reading the method name.
 *
 *   → @Query lets us write our own JPQL (Java Persistence Query Language)
 *     when we need something custom.
 *
 * ──────────────────────────────────────────────────────────────────────────
 * ANALYTICS ADDITIONS (new methods added at the bottom of this file)
 * ──────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Existing queries (do NOT change these) ───────────────────────────────

    List<Product> findByAvailableTrue();

    List<Product> findByCategoryIgnoreCase(String category);

    @Query("SELECT p FROM Product p WHERE p.available = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findByAvailableTrueAndCategoryIgnoreCase(String category);

    // ── NEW: Analytics queries ────────────────────────────────────────────────

    /**
     * Count all products (available + unavailable).
     *
     * Why: The admin wants to know how many products exist in total,
     * not just how many are currently visible to customers.
     *
     * Note: JpaRepository already provides count() which counts ALL rows,
     * so this method isn't strictly necessary — but it's here to show
     * intentional analytics design.  We call repository.count() directly
     * in the service instead.
     */
    // (No extra method needed — repository.count() from JpaRepository works.)

    /**
     * Find all products ordered by how many times they appear in order_items,
     * descending.  We return Object[] arrays where:
     *   index 0 → the Product entity
     *   index 1 → the total quantity ordered (Long)
     *
     * JPQL explanation:
     *   SELECT p, SUM(oi.quantity)   ← select the product + total qty sold
     *   FROM OrderItem oi            ← start from the order items table
     *   JOIN oi.product p            ← join to get the product details
     *   GROUP BY p                   ← group by each product
     *   ORDER BY SUM(...) DESC       ← highest-selling first
     */
    @Query("SELECT p, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "GROUP BY p " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findProductsSortedByDemand();

    /**
     * Find ONLY the single most-ordered product (LIMIT 1 via Spring's Pageable
     * or by using the query below with a sub-select trick).
     *
     * We reuse findProductsSortedByDemand() and take the first element
     * in the service layer — simpler and easier to understand.
     */
}
