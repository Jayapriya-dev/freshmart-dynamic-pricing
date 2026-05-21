package com.grocery.store.service;

import com.grocery.store.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * PricingService — Rule-Based Dynamic Pricing Engine
 *
 * Rules applied in order:
 *  1. Admin-set discount percentage
 *  2. Low stock surcharge  (stock < 10  → +10%)
 *  3. High stock discount  (stock > 100 → -5%)
 *  4. Weekend discount     (Sat/Sun     → -5%)
 */
@Service
@Slf4j
public class PricingService {

    private static final int    LOW_STOCK_THRESHOLD  = 10;
    private static final int    HIGH_STOCK_THRESHOLD = 100;
    private static final double LOW_STOCK_SURCHARGE  = 0.10;  // +10%
    private static final double HIGH_STOCK_DISCOUNT  = 0.05;  // -5%
    private static final double WEEKEND_DISCOUNT     = 0.05;  // -5%

    /**
     * Calculate the final price for a product applying all pricing rules.
     *
     * @param product the product whose price is to be calculated
     * @return the final price after all rules, rounded to 2 decimal places
     */
    public BigDecimal calculateFinalPrice(Product product) {
        BigDecimal price = product.getBasePrice();

        // Rule 1: Apply admin-set discount percentage
        if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
            BigDecimal adminDiscount = BigDecimal.valueOf(product.getDiscountPercentage() / 100.0);
            price = price.subtract(price.multiply(adminDiscount));
            log.debug("After admin discount ({}%): {}", product.getDiscountPercentage(), price);
        }

        // Rule 2: Low stock surcharge — if stock < 10, increase by 10%
        if (product.getStockQuantity() < LOW_STOCK_THRESHOLD) {
            BigDecimal surcharge = price.multiply(BigDecimal.valueOf(LOW_STOCK_SURCHARGE));
            price = price.add(surcharge);
            log.debug("Low stock surcharge applied (+10%): {}", price);
        }

        // Rule 3: High stock discount — if stock > 100, decrease by 5%
        else if (product.getStockQuantity() > HIGH_STOCK_THRESHOLD) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(HIGH_STOCK_DISCOUNT));
            price = price.subtract(discount);
            log.debug("High stock discount applied (-5%): {}", price);
        }

        // Rule 4: Weekend discount — Saturday or Sunday, extra -5%
        if (isWeekend()) {
            BigDecimal weekendDiscount = price.multiply(BigDecimal.valueOf(WEEKEND_DISCOUNT));
            price = price.subtract(weekendDiscount);
            log.debug("Weekend discount applied (-5%): {}", price);
        }

        // Ensure price never goes below 0
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            price = BigDecimal.ZERO;
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generate a human-readable breakdown of how the price was calculated.
     *
     * @param product the product
     * @return descriptive pricing breakdown string
     */
    public String getPriceBreakdown(Product product) {
        StringBuilder sb = new StringBuilder();
        BigDecimal base = product.getBasePrice();
        sb.append(String.format("Base Price: ₹%.2f", base));

        if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
            sb.append(String.format(" | Admin Discount: -%.1f%%", product.getDiscountPercentage()));
        }

        if (product.getStockQuantity() < LOW_STOCK_THRESHOLD) {
            sb.append(" | Low Stock Surcharge: +10%");
        } else if (product.getStockQuantity() > HIGH_STOCK_THRESHOLD) {
            sb.append(" | High Stock Discount: -5%");
        }

        if (isWeekend()) {
            sb.append(" | Weekend Discount: -5%");
        }

        BigDecimal finalPrice = calculateFinalPrice(product);
        sb.append(String.format(" | Final Price: ₹%.2f", finalPrice));

        return sb.toString();
    }

    private boolean isWeekend() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
