package com.promox.flashsale.scheduler;

import com.promox.flashsale.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashSaleScheduler {

    private final FlashSaleService flashSaleService;

    /**
     * Start scheduled flash sales every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void startScheduledFlashSales() {
        log.info("Running scheduled task: Start flash sales");
        flashSaleService.startScheduledFlashSales();
    }

    /**
     * End expired flash sales every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void endExpiredFlashSales() {
        log.info("Running scheduled task: End expired flash sales");
        flashSaleService.endExpiredFlashSales();
    }

    /**
     * Sync Redis stock to database every 5 minutes
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void syncStockToDatabase() {
        log.info("Running scheduled task: Sync stock to database");
        flashSaleService.syncAllStockToDatabase();
    }
}
