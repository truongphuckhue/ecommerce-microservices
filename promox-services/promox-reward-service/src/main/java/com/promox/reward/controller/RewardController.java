package com.promox.reward.controller;

import com.promox.reward.dto.*;
import com.promox.reward.entity.*;
import com.promox.reward.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reward Management", description = "APIs for managing reward points, tiers, and achievements")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/account/{userId}")
    @Operation(summary = "Get reward account", description = "Get or create reward account for user")
    public ResponseEntity<ApiResponse<RewardAccountResponse>> getAccount(@PathVariable Long userId) {
        log.info("REST request to get reward account for user: {}", userId);

        RewardAccountResponse response = rewardService.getOrCreateAccount(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/earn")
    @Operation(summary = "Earn points", description = "Award points to user for various activities")
    public ResponseEntity<ApiResponse<RewardAccountResponse>> earnPoints(
            @Valid @RequestBody EarnPointsRequest request) {

        log.info("REST request to earn points for user: {}", request.getUserId());

        RewardAccountResponse response = rewardService.earnPoints(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Points earned successfully", response));
    }

    @PostMapping("/redeem")
    @Operation(summary = "Redeem points", description = "Redeem points for rewards")
    public ResponseEntity<ApiResponse<RewardAccountResponse>> redeemPoints(
            @Valid @RequestBody RedeemPointsRequest request) {

        log.info("REST request to redeem points for user: {}", request.getUserId());

        RewardAccountResponse response = rewardService.redeemPoints(request);

        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully", response));
    }

    @PostMapping("/transactions/{transactionId}/confirm")
    @Operation(summary = "Confirm pending points", description = "Confirm pending points transaction")
    public ResponseEntity<ApiResponse<Void>> confirmPendingPoints(@PathVariable Long transactionId) {
        log.info("REST request to confirm transaction: {}", transactionId);

        rewardService.confirmPendingPoints(transactionId);

        return ResponseEntity.ok(ApiResponse.success("Transaction confirmed successfully", null));
    }

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "Get transaction history", description = "Get points transaction history for user")
    public ResponseEntity<ApiResponse<Page<PointTransaction>>> getTransactionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get transaction history for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PointTransaction> transactions = rewardService.getTransactionHistory(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/achievements")
    @Operation(summary = "Get all achievements", description = "Get all available achievements")
    public ResponseEntity<ApiResponse<List<Achievement>>> getAllAchievements() {
        log.info("REST request to get all achievements");

        List<Achievement> achievements = rewardService.getAllAchievements();

        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    @GetMapping("/achievements/{userId}")
    @Operation(summary = "Get user achievements", description = "Get achievements for specific user")
    public ResponseEntity<ApiResponse<List<UserAchievement>>> getUserAchievements(
            @PathVariable Long userId) {

        log.info("REST request to get achievements for user: {}", userId);

        List<UserAchievement> userAchievements = rewardService.getUserAchievements(userId);

        return ResponseEntity.ok(ApiResponse.success(userAchievements));
    }

    @PostMapping("/achievements/{userId}/check")
    @Operation(summary = "Check achievements", description = "Check and unlock new achievements for user")
    public ResponseEntity<ApiResponse<Void>> checkAchievements(@PathVariable Long userId) {
        log.info("REST request to check achievements for user: {}", userId);

        rewardService.checkAndUnlockAchievements(userId);

        return ResponseEntity.ok(ApiResponse.success("Achievements checked successfully", null));
    }

    @PostMapping("/tier/{userId}/update")
    @Operation(summary = "Update tier", description = "Update user tier level based on points")
    public ResponseEntity<ApiResponse<Void>> updateTier(@PathVariable Long userId) {
        log.info("REST request to update tier for user: {}", userId);

        rewardService.updateTierLevel(userId);

        return ResponseEntity.ok(ApiResponse.success("Tier updated successfully", null));
    }
}
