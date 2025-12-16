package com.promox.analytics.mapper;

import com.promox.analytics.dto.*;
import com.promox.analytics.entity.*;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMapper {

    public CampaignPerformanceResponse toCampaignPerformanceResponse(CampaignAnalytics analytics) {
        if (analytics == null) return null;

        return CampaignPerformanceResponse.builder()
                .campaignId(analytics.getCampaignId())
                .analyticsDate(analytics.getAnalyticsDate())
                .totalOrders(analytics.getTotalOrders())
                .totalRevenue(analytics.getTotalRevenue())
                .totalDiscount(analytics.getTotalDiscount())
                .uniqueUsers(analytics.getUniqueUsers())
                .impressions(analytics.getImpressions())
                .clicks(analytics.getClicks())
                .conversionRate(analytics.getConversionRate())
                .clickThroughRate(analytics.getClickThroughRate())
                .avgOrderValue(analytics.getAvgOrderValue())
                .avgDiscount(analytics.getAvgDiscount())
                .discountPercentage(analytics.getDiscountPercentage())
                .build();
    }

    public PromotionMetricsResponse toPromotionMetricsResponse(PromotionAnalytics analytics) {
        if (analytics == null) return null;

        return PromotionMetricsResponse.builder()
                .promotionId(analytics.getPromotionId())
                .analyticsDate(analytics.getAnalyticsDate())
                .usageCount(analytics.getUsageCount())
                .uniqueUsers(analytics.getUniqueUsers())
                .totalDiscount(analytics.getTotalDiscount())
                .totalRevenue(analytics.getTotalRevenue())
                .netRevenue(analytics.getNetRevenue())
                .avgOrderValue(analytics.getAvgOrderValue())
                .roi(analytics.getRoi())
                .discountPercentage(analytics.getDiscountPercentage())
                .build();
    }
}
