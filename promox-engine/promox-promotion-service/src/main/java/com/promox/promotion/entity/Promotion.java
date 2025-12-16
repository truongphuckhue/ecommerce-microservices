package com.promox.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PromotionType type;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20)
    private DiscountType discountType;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "applicable_products", columnDefinition = "jsonb")
    private String applicableProducts;

    @Column(name = "applicable_categories", columnDefinition = "jsonb")
    private String applicableCategories;

    @Column(name = "excluded_products", columnDefinition = "jsonb")
    private String excludedProducts;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean stackable = false;

    @Column
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PromotionStatus status = PromotionStatus.DRAFT;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isActive() {
        return status == PromotionStatus.ACTIVE
                && validFrom.isBefore(LocalDateTime.now())
                && validTo.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        return validTo.isBefore(LocalDateTime.now());
    }

    public boolean isExhausted() {
        return usageLimit != null && usageCount >= usageLimit;
    }

    public boolean canBeUsed() {
        return isActive() && !isExhausted();
    }

    public Integer getRemainingUsage() {
        if (usageLimit == null) return null;
        return Math.max(0, usageLimit - usageCount);
    }

    public BigDecimal getUsagePercentage() {
        if (usageLimit == null || usageLimit == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(usageCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(usageLimit), 2, BigDecimal.ROUND_HALF_UP);
    }

    // Enums
    public enum PromotionType {
        PERCENTAGE,      // 20% off
        FIXED_AMOUNT,    // $10 off
        BUY_X_GET_Y,     // Buy 2 Get 1
        FREE_SHIPPING,   // Free shipping
        BUNDLE           // Bundle deal
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }

    public enum PromotionStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        EXPIRED,
        EXHAUSTED
    }
}
