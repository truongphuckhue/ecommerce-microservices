package com.promox.reward.dto;

import com.promox.reward.entity.RewardAccount;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardAccountResponse {
    private Long id;
    private Long userId;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer pendingPoints;
    private Integer redeemedPoints;
    private Integer expiredPoints;
    private RewardAccount.TierLevel tierLevel;
    private Integer tierProgress;
    private Integer nextTierPoints;
    private Integer pointsToNextTier;
    private BigDecimal totalEarned;
    private BigDecimal totalRedeemed;
    private BigDecimal lifetimeValue;
    private BigDecimal pointsValueInCurrency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
}
