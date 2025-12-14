package com.promox.reward.service;

import com.promox.reward.dto.*;
import com.promox.reward.entity.*;
import com.promox.reward.exception.RewardAccountNotFoundException;
import com.promox.reward.mapper.RewardMapper;
import com.promox.reward.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardServiceImpl implements RewardService {

    private final RewardAccountRepository accountRepository;
    private final PointTransactionRepository transactionRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final RewardMapper rewardMapper;

    // Tier thresholds
    private static final Map<RewardAccount.TierLevel, Integer> TIER_THRESHOLDS = Map.of(
        RewardAccount.TierLevel.BRONZE, 0,
        RewardAccount.TierLevel.SILVER, 1000,
        RewardAccount.TierLevel.GOLD, 3000,
        RewardAccount.TierLevel.PLATINUM, 7000,
        RewardAccount.TierLevel.DIAMOND, 15000
    );

    @Override
    @Transactional
    public RewardAccountResponse getOrCreateAccount(Long userId) {
        log.info("Getting or creating reward account for user: {}", userId);

        Optional<RewardAccount> existingAccount = accountRepository.findByUserId(userId);
        
        if (existingAccount.isPresent()) {
            return rewardMapper.toAccountResponse(existingAccount.get());
        }

        // Create new account
        RewardAccount newAccount = RewardAccount.builder()
                .userId(userId)
                .totalPoints(0)
                .availablePoints(0)
                .tierLevel(RewardAccount.TierLevel.BRONZE)
                .tierProgress(0)
                .nextTierPoints(TIER_THRESHOLDS.get(RewardAccount.TierLevel.SILVER))
                .build();

        RewardAccount savedAccount = accountRepository.save(newAccount);
        
        log.info("Created new reward account for user: {}", userId);

        return rewardMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public RewardAccountResponse getAccountByUserId(Long userId) {
        log.info("Fetching reward account for user: {}", userId);

        RewardAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RewardAccountNotFoundException(userId));

        return rewardMapper.toAccountResponse(account);
    }

    @Override
    @Transactional
    public RewardAccountResponse earnPoints(EarnPointsRequest request) {
        log.info("User {} earning {} points", request.getUserId(), request.getPoints());

        // Get or create account
        RewardAccount account = accountRepository.findByUserId(request.getUserId())
                .orElseGet(() -> {
                    RewardAccount newAccount = RewardAccount.builder()
                            .userId(request.getUserId())
                            .tierLevel(RewardAccount.TierLevel.BRONZE)
                            .nextTierPoints(TIER_THRESHOLDS.get(RewardAccount.TierLevel.SILVER))
                            .build();
                    return accountRepository.save(newAccount);
                });

        // Calculate expiry
        LocalDateTime expiresAt = null;
        if (request.getExpiryDays() != null && request.getExpiryDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpiryDays());
        }

        // Create transaction (PENDING if has expiry, CONFIRMED otherwise)
        PointTransaction.TransactionStatus status = expiresAt != null 
                ? PointTransaction.TransactionStatus.PENDING 
                : PointTransaction.TransactionStatus.CONFIRMED;

        PointTransaction transaction = PointTransaction.builder()
                .userId(request.getUserId())
                .transactionType(request.getTransactionType())
                .points(request.getPoints())
                .balanceAfter(account.getAvailablePoints())
                .status(status)
                .description(request.getDescription())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .expiresAt(expiresAt)
                .build();

        if (status == PointTransaction.TransactionStatus.CONFIRMED) {
            transaction.setConfirmedAt(LocalDateTime.now());
            // Credit immediately
            account.earnPoints(request.getPoints());
            transaction.setBalanceAfter(account.getAvailablePoints());
        } else {
            // Add to pending
            account.addPendingPoints(request.getPoints());
        }

        transactionRepository.save(transaction);
        
        // Update tier level
        updateTierLevel(request.getUserId());
        
        // Check achievements
        checkAndUnlockAchievements(request.getUserId());

        RewardAccount updatedAccount = accountRepository.save(account);

        log.info("Points earned successfully. New balance: {}", updatedAccount.getAvailablePoints());

        return rewardMapper.toAccountResponse(updatedAccount);
    }

    @Override
    @Transactional
    public RewardAccountResponse redeemPoints(RedeemPointsRequest request) {
        log.info("User {} redeeming {} points", request.getUserId(), request.getPoints());

        RewardAccount account = accountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RewardAccountNotFoundException(request.getUserId()));

        // Validate sufficient points
        if (account.getAvailablePoints() < request.getPoints()) {
            throw new IllegalStateException("Insufficient points. Available: " + 
                    account.getAvailablePoints() + ", Required: " + request.getPoints());
        }

        // Deduct points
        account.redeemPoints(request.getPoints());

        // Create redemption transaction
        PointTransaction transaction = PointTransaction.builder()
                .userId(request.getUserId())
                .transactionType(PointTransaction.TransactionType.REDEMPTION)
                .points(-request.getPoints())  // Negative for redemption
                .balanceAfter(account.getAvailablePoints())
                .status(PointTransaction.TransactionStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now())
                .description(request.getDescription())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .build();

        transactionRepository.save(transaction);

        RewardAccount updatedAccount = accountRepository.save(account);

        log.info("Points redeemed successfully. New balance: {}", updatedAccount.getAvailablePoints());

        return rewardMapper.toAccountResponse(updatedAccount);
    }

    @Override
    @Transactional
    public void confirmPendingPoints(Long transactionId) {
        log.info("Confirming pending transaction: {}", transactionId);

        PointTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != PointTransaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending");
        }

        RewardAccount account = accountRepository.findByUserId(transaction.getUserId())
                .orElseThrow(() -> new RewardAccountNotFoundException(transaction.getUserId()));

        // Confirm points
        account.confirmPendingPoints(transaction.getPoints());
        transaction.setStatus(PointTransaction.TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(LocalDateTime.now());
        transaction.setBalanceAfter(account.getAvailablePoints());

        transactionRepository.save(transaction);
        accountRepository.save(account);

        // Update tier
        updateTierLevel(transaction.getUserId());

        log.info("Pending points confirmed. User {} now has {} points", 
                transaction.getUserId(), account.getAvailablePoints());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistory(Long userId, Pageable pageable) {
        log.info("Fetching transaction history for user: {}", userId);

        return transactionRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> getAllAchievements() {
        log.info("Fetching all active achievements");

        return achievementRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievements(Long userId) {
        log.info("Fetching achievements for user: {}", userId);

        return userAchievementRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void checkAndUnlockAchievements(Long userId) {
        log.info("Checking achievements for user: {}", userId);

        List<Achievement> allAchievements = achievementRepository.findByActiveTrue();

        for (Achievement achievement : allAchievements) {
            // Check if user already has this achievement
            Optional<UserAchievement> existing = userAchievementRepository
                    .findByUserIdAndAchievementId(userId, achievement.getId());

            if (existing.isPresent() && existing.get().getIsCompleted()) {
                continue; // Already unlocked
            }

            // Check if achievement criteria met
            boolean criteriaMetAndReward = checkAchievementCriteria(userId, achievement);

            if (criteriaMetAndReward) {
                unlockAchievement(userId, achievement);
            }
        }
    }

    @Override
    @Transactional
    public void updateTierLevel(Long userId) {
        log.info("Updating tier level for user: {}", userId);

        RewardAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RewardAccountNotFoundException(userId));

        Integer totalPoints = account.getTotalPoints();
        RewardAccount.TierLevel currentTier = account.getTierLevel();
        RewardAccount.TierLevel newTier = calculateTierLevel(totalPoints);

        if (newTier != currentTier) {
            log.info("User {} tier upgraded from {} to {}", userId, currentTier, newTier);

            account.setTierLevel(newTier);
            
            // Give tier upgrade bonus
            int bonusPoints = getTierUpgradeBonus(newTier);
            if (bonusPoints > 0) {
                account.earnPoints(bonusPoints);

                PointTransaction bonusTransaction = PointTransaction.builder()
                        .userId(userId)
                        .transactionType(PointTransaction.TransactionType.TIER_UPGRADE_BONUS)
                        .points(bonusPoints)
                        .balanceAfter(account.getAvailablePoints())
                        .status(PointTransaction.TransactionStatus.CONFIRMED)
                        .confirmedAt(LocalDateTime.now())
                        .description("Tier upgrade bonus: " + newTier)
                        .build();

                transactionRepository.save(bonusTransaction);
            }
        }

        // Update tier progress and next tier
        account.setTierProgress(totalPoints);
        RewardAccount.TierLevel nextTier = getNextTier(newTier);
        if (nextTier != null) {
            account.setNextTierPoints(TIER_THRESHOLDS.get(nextTier));
        } else {
            account.setNextTierPoints(null); // Already at max tier
        }

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void expirePendingTransactions() {
        log.info("Running scheduled task to expire pending transactions");

        List<PointTransaction> expiredTransactions = transactionRepository
                .findExpiredPendingTransactions(LocalDateTime.now());

        for (PointTransaction transaction : expiredTransactions) {
            RewardAccount account = accountRepository.findByUserId(transaction.getUserId())
                    .orElse(null);

            if (account != null) {
                // Remove from pending
                if (account.getPendingPoints() >= transaction.getPoints()) {
                    account.setPendingPoints(account.getPendingPoints() - transaction.getPoints());
                    accountRepository.save(account);
                }
            }

            // Mark as expired
            transaction.setStatus(PointTransaction.TransactionStatus.EXPIRED);
            transactionRepository.save(transaction);

            log.info("Expired pending transaction {} for user {}", 
                    transaction.getId(), transaction.getUserId());
        }

        log.info("Expired {} pending transactions", expiredTransactions.size());
    }

    // Helper methods

    private boolean checkAchievementCriteria(Long userId, Achievement achievement) {
        switch (achievement.getAchievementType()) {
            case FIRST_PURCHASE:
                Integer purchaseCount = transactionRepository.sumPointsByUserAndType(
                        userId, PointTransaction.TransactionType.PURCHASE_REWARD);
                return purchaseCount != null && purchaseCount > 0;

            case PURCHASE_MILESTONE:
                // Check if required count met
                return false; // Implement based on your business logic

            default:
                return false;
        }
    }

    private void unlockAchievement(Long userId, Achievement achievement) {
        log.info("Unlocking achievement {} for user {}", achievement.getName(), userId);

        UserAchievement userAchievement = UserAchievement.builder()
                .userId(userId)
                .achievementId(achievement.getId())
                .achievementCode(achievement.getCode())
                .progress(achievement.getRequiredCount())
                .requiredCount(achievement.getRequiredCount())
                .isCompleted(true)
                .pointsEarned(achievement.getPointsReward())
                .completedAt(LocalDateTime.now())
                .build();

        userAchievementRepository.save(userAchievement);

        // Award points
        if (achievement.getPointsReward() > 0) {
            RewardAccount account = accountRepository.findByUserId(userId).orElse(null);
            if (account != null) {
                account.earnPoints(achievement.getPointsReward());

                PointTransaction transaction = PointTransaction.builder()
                        .userId(userId)
                        .transactionType(PointTransaction.TransactionType.ACHIEVEMENT_REWARD)
                        .points(achievement.getPointsReward())
                        .balanceAfter(account.getAvailablePoints())
                        .status(PointTransaction.TransactionStatus.CONFIRMED)
                        .confirmedAt(LocalDateTime.now())
                        .description("Achievement unlocked: " + achievement.getName())
                        .referenceType("ACHIEVEMENT")
                        .referenceId(achievement.getId().toString())
                        .build();

                transactionRepository.save(transaction);
                accountRepository.save(account);
            }
        }

        log.info("Achievement unlocked successfully");
    }

    private RewardAccount.TierLevel calculateTierLevel(Integer totalPoints) {
        if (totalPoints >= TIER_THRESHOLDS.get(RewardAccount.TierLevel.DIAMOND)) {
            return RewardAccount.TierLevel.DIAMOND;
        } else if (totalPoints >= TIER_THRESHOLDS.get(RewardAccount.TierLevel.PLATINUM)) {
            return RewardAccount.TierLevel.PLATINUM;
        } else if (totalPoints >= TIER_THRESHOLDS.get(RewardAccount.TierLevel.GOLD)) {
            return RewardAccount.TierLevel.GOLD;
        } else if (totalPoints >= TIER_THRESHOLDS.get(RewardAccount.TierLevel.SILVER)) {
            return RewardAccount.TierLevel.SILVER;
        } else {
            return RewardAccount.TierLevel.BRONZE;
        }
    }

    private RewardAccount.TierLevel getNextTier(RewardAccount.TierLevel currentTier) {
        switch (currentTier) {
            case BRONZE: return RewardAccount.TierLevel.SILVER;
            case SILVER: return RewardAccount.TierLevel.GOLD;
            case GOLD: return RewardAccount.TierLevel.PLATINUM;
            case PLATINUM: return RewardAccount.TierLevel.DIAMOND;
            case DIAMOND: return null; // Max tier
            default: return null;
        }
    }

    private int getTierUpgradeBonus(RewardAccount.TierLevel tier) {
        switch (tier) {
            case SILVER: return 100;
            case GOLD: return 300;
            case PLATINUM: return 700;
            case DIAMOND: return 1500;
            default: return 0;
        }
    }
}
