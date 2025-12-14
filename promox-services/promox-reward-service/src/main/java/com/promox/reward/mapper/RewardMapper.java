package com.promox.reward.mapper;

import com.promox.reward.dto.RewardAccountResponse;
import com.promox.reward.entity.RewardAccount;
import org.springframework.stereotype.Component;

@Component
public class RewardMapper {

    public RewardAccountResponse toAccountResponse(RewardAccount account) {
        if (account == null) return null;

        return RewardAccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .totalPoints(account.getTotalPoints())
                .availablePoints(account.getAvailablePoints())
                .pendingPoints(account.getPendingPoints())
                .redeemedPoints(account.getRedeemedPoints())
                .expiredPoints(account.getExpiredPoints())
                .tierLevel(account.getTierLevel())
                .tierProgress(account.getTierProgress())
                .nextTierPoints(account.getNextTierPoints())
                .pointsToNextTier(account.getPointsToNextTier())
                .totalEarned(account.getTotalEarned())
                .totalRedeemed(account.getTotalRedeemed())
                .lifetimeValue(account.getLifetimeValue())
                .pointsValueInCurrency(account.getPointsValueInCurrency())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .lastActivityAt(account.getLastActivityAt())
                .build();
    }
}
