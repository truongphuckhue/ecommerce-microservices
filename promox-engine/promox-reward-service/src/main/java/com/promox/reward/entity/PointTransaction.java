package com.promox.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "transaction_type"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String description;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // Enums
    public enum TransactionType {
        PURCHASE_REWARD,        // Points earned from purchases
        SIGNUP_BONUS,          // Welcome bonus
        REFERRAL_BONUS,        // Referral reward
        BIRTHDAY_BONUS,        // Birthday gift
        ACHIEVEMENT_REWARD,    // Achievement unlock
        TIER_UPGRADE_BONUS,    // Tier upgrade bonus
        ADMIN_ADJUSTMENT,      // Manual adjustment
        REDEMPTION,            // Points spent
        EXPIRATION,            // Points expired
        REFUND                 // Points refunded
    }

    public enum TransactionStatus {
        PENDING,       // Waiting for confirmation
        CONFIRMED,     // Confirmed and credited
        EXPIRED,       // Expired before confirmation
        CANCELLED      // Cancelled
    }
}
