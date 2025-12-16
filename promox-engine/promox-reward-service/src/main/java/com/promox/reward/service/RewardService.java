package com.promox.reward.service;

import com.promox.reward.dto.*;
import com.promox.reward.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RewardService {
    // Account management
    RewardAccountResponse getOrCreateAccount(Long userId);
    RewardAccountResponse getAccountByUserId(Long userId);
    
    // Points operations
    RewardAccountResponse earnPoints(EarnPointsRequest request);
    RewardAccountResponse redeemPoints(RedeemPointsRequest request);
    void confirmPendingPoints(Long transactionId);
    
    // Transactions
    Page<PointTransaction> getTransactionHistory(Long userId, Pageable pageable);
    
    // Achievements
    List<Achievement> getAllAchievements();
    List<UserAchievement> getUserAchievements(Long userId);
    void checkAndUnlockAchievements(Long userId);
    
    // Tier management
    void updateTierLevel(Long userId);
    
    // Scheduled tasks
    void expirePendingTransactions();
}
