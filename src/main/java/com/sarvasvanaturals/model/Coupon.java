package com.sarvasvanaturals.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private int usedCount = 0;

    private LocalDate validFrom;
    private LocalDate validUntil;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public boolean isValid(BigDecimal orderAmount) {
        if (!active) return false;
        if (validFrom != null && LocalDate.now().isBefore(validFrom)) return false;
        if (validUntil != null && LocalDate.now().isAfter(validUntil)) return false;
        if (usageLimit != null && usedCount >= usageLimit) return false;
        if (minimumOrderAmount != null && orderAmount.compareTo(minimumOrderAmount) < 0) return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal discount;
        if (type == CouponType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
                discount = maximumDiscountAmount;
            }
        } else {
            discount = discountValue;
        }
        return discount.min(orderAmount);
    }

    public enum CouponType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
