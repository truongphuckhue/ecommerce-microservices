package com.promox.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_redemptions", indexes = {
    @Index(name = "idx_coupon_id", columnList = "coupon_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_order_id", columnList = "order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "coupon_code", nullable = false, length = 50)
    private String couponCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RedemptionStatus status = RedemptionStatus.SUCCESS;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String metadata;

    @CreatedDate
    @Column(name = "redeemed_at", updatable = false)
    private LocalDateTime redeemedAt;

    // Enum
    public enum RedemptionStatus {
        SUCCESS,
        FAILED,
        REVERSED
    }
}
