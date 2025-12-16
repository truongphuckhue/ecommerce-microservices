package com.promox.analytics.scheduler;

import com.promox.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;

    @Scheduled(cron = "0 0 0 * * *")  // Every day at midnight
    public void aggregateDailyAnalytics() {
        log.info("Running scheduled task: Daily analytics aggregation");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            analyticsService.aggregateDailyAnalytics(yesterday);
            log.info("Daily analytics aggregation completed successfully for {}", yesterday);
        } catch (Exception e) {
            log.error("Error during daily analytics aggregation", e);
        }
    }
}
