package com.promox.coupon.config;

import com.promox.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final CouponService couponService;

    /**
     * Update expired coupons every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateExpiredCoupons() {
        log.info("Running scheduled task: Update expired coupons");
        couponService.updateExpiredCoupons();
    }

    /**
     * Update exhausted coupons every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void updateExhaustedCoupons() {
        log.info("Running scheduled task: Update exhausted coupons");
        couponService.updateExhaustedCoupons();
    }
}
