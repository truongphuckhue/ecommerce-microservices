package com.promox.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RewardAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "available_points", nullable = false)
    @Builder.Default
    private Integer availablePoints = 0;

    @Column(name = "pending_points", nullable = false)
    @Builder.Default
    private Integer pendingPoints = 0;

    @Column(name = "redeemed_points", nullable = false)
    @Builder.Default
    private Integer redeemedPoints = 0;

    @Column(name = "expired_points", nullable = false)
    @Builder.Default
    private Integer expiredPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 20)
    @Builder.Default
    private TierLevel tierLevel = TierLevel.BRONZE;

    @Column(name = "tier_progress", nullable = false)
    @Builder.Default
    private Integer tierProgress = 0;

    @Column(name = "next_tier_points")
    private Integer nextTierPoints;

    @Column(name = "total_earned", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_redeemed", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRedeemed = BigDecimal.ZERO;

    @Column(name = "lifetime_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lifetimeValue = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    // Business methods
    public void earnPoints(Integer points) {
        this.availablePoints += points;
        this.totalPoints += points;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void redeemPoints(Integer points) {
        if (this.availablePoints < points) {
            throw new IllegalStateException("Insufficient points");
        }
        this.availablePoints -= points;
        this.redeemedPoints += points;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void addPendingPoints(Integer points) {
        this.pendingPoints += points;
    }

    public void confirmPendingPoints(Integer points) {
        if (this.pendingPoints < points) {
            throw new IllegalStateException("Insufficient pending points");
        }
        this.pendingPoints -= points;
        this.availablePoints += points;
        this.totalPoints += points;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void expirePoints(Integer points) {
        if (this.availablePoints < points) {
            points = this.availablePoints;
        }
        this.availablePoints -= points;
        this.expiredPoints += points;
    }

    public BigDecimal getPointsValueInCurrency() {
        // 100 points = $1
        return BigDecimal.valueOf(availablePoints).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    public Integer getPointsToNextTier() {
        if (nextTierPoints == null) return null;
        return Math.max(0, nextTierPoints - tierProgress);
    }

    // Enums
    public enum TierLevel {
        BRONZE,      // 0-999 points
        SILVER,      // 1000-2999 points
        GOLD,        // 3000-6999 points
        PLATINUM,    // 7000-14999 points
        DIAMOND      // 15000+ points
    }
}
