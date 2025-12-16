package com.promox.reward.exception;

public class RewardAccountNotFoundException extends RuntimeException {
    public RewardAccountNotFoundException(Long userId) {
        super("Reward account not found for user: " + userId);
    }
}
