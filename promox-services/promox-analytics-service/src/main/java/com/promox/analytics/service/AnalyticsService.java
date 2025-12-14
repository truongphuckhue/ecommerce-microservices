package com.promox.analytics.service;

import com.promox.analytics.dto.*;
import com.promox.analytics.entity.*;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    
    // Usage Logging
    PromotionUsageLog recordUsage(RecordUsageRequest request);
    
    // Campaign Analytics
    CampaignPerformanceResponse getCampaignPerformance(Long campaignId, LocalDate date);
    List<CampaignPerformanceResponse> getCampaignPerformanceRange(Long campaignId, LocalDate startDate, LocalDate endDate);
    void recordCampaignImpression(Long campaignId);
    void recordCampaignClick(Long campaignId);
    
    // Promotion Analytics
    PromotionMetricsResponse getPromotionMetrics(Long promotionId, LocalDate date);
    List<PromotionMetricsResponse> getPromotionMetricsRange(Long promotionId, LocalDate startDate, LocalDate endDate);
    
    // Dashboard & Reports
    DashboardSummaryResponse getDashboardSummary(LocalDate date);
    List<CampaignPerformanceResponse> getTopCampaigns(LocalDate date, int limit);
    List<PromotionMetricsResponse> getTopPromotions(LocalDate date, int limit);
    
    // Aggregation (Scheduled Jobs)
    void aggregateDailyAnalytics(LocalDate date);
    void aggregateCampaignAnalytics(Long campaignId, LocalDate date);
    void aggregatePromotionAnalytics(Long promotionId, LocalDate date);
}
