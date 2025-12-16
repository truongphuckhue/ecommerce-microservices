package com.promox.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupon_code", columnList = "code"),
    @Index(name = "idx_campaign_id", columnList = "campaign_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 20)
    private CouponType couponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.ACTIVE;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    // Business methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CouponStatus.ACTIVE
                && now.isAfter(validFrom)
                && now.isBefore(validTo);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validTo);
    }

    public boolean isExhausted() {
        return usageLimit != null && usageCount >= usageLimit;
    }

    public boolean canBeUsed() {
        return isValid() && !isExhausted();
    }

    public boolean isAssignedTo(Long userId) {
        return assignedUserId != null && assignedUserId.equals(userId);
    }

    public Integer getRemainingUsage() {
        if (usageLimit == null) return Integer.MAX_VALUE;
        return Math.max(0, usageLimit - usageCount);
    }

    public BigDecimal getUsagePercentage() {
        if (usageLimit == null || usageLimit == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(usageCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(usageLimit), 2, BigDecimal.ROUND_HALF_UP);
    }

    // Enums
    public enum CouponType {
        GENERIC,        // Anyone can use
        PERSONALIZED,   // Assigned to specific user
        REFERRAL,       // Referral program
        LOYALTY,        // Loyalty reward
        FIRST_ORDER,    // First-time customer
        BIRTHDAY        // Birthday coupon
    }

    public enum CouponStatus {
        ACTIVE,         // Available for use
        INACTIVE,       // Not available
        EXPIRED,        // Past valid_to date
        EXHAUSTED,      // All usage consumed
        REVOKED         // Manually revoked
    }

    public enum DiscountType {
        PERCENTAGE,     // Percentage off
        FIXED           // Fixed amount off
    }
}
