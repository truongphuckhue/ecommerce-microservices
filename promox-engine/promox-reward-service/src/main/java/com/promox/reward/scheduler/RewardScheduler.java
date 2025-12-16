package com.promox.reward.scheduler;

import com.promox.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RewardScheduler {

    private final RewardService rewardService;

    @Scheduled(cron = "0 0 * * * *")  // Every hour
    public void expirePendingTransactions() {
        log.info("Running scheduled task: Expire pending transactions");
        rewardService.expirePendingTransactions();
    }
}
