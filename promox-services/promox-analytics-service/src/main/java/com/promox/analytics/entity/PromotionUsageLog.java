package com.promox.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_usage_log", indexes = {
    @Index(name = "idx_usage_log_promotion", columnList = "promotion_id"),
    @Index(name = "idx_usage_log_user", columnList = "user_id"),
    @Index(name = "idx_usage_log_date", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PromotionUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "discount_applied", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountApplied;

    @Column(name = "order_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal orderAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public BigDecimal getFinalAmount() {
        return orderAmount.subtract(discountApplied);
    }

    public BigDecimal getDiscountPercentage() {
        if (orderAmount.compareTo(BigDecimal.ZERO) > 0) {
            return discountApplied
                .multiply(BigDecimal.valueOf(100))
                .divide(orderAmount, 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
