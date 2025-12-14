package com.promox.analytics.service;

import com.promox.analytics.dto.*;
import com.promox.analytics.entity.*;
import com.promox.analytics.mapper.AnalyticsMapper;
import com.promox.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final CampaignAnalyticsRepository campaignAnalyticsRepository;
    private final PromotionAnalyticsRepository promotionAnalyticsRepository;
    private final PromotionUsageLogRepository usageLogRepository;
    private final AnalyticsMapper analyticsMapper;

    @Override
    @Transactional
    public PromotionUsageLog recordUsage(RecordUsageRequest request) {
        log.info("Recording promotion usage: promotionId={}, userId={}, amount={}",
                request.getPromotionId(), request.getUserId(), request.getOrderAmount());

        PromotionUsageLog usageLog = PromotionUsageLog.builder()
                .promotionId(request.getPromotionId())
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .discountApplied(request.getDiscountApplied())
                .orderAmount(request.getOrderAmount())
                .success(request.getSuccess())
                .failureReason(request.getFailureReason())
                .ipAddress(request.getIpAddress())
                .build();

        PromotionUsageLog savedLog = usageLogRepository.save(usageLog);

        // Real-time aggregation for today's data
        if (request.getSuccess()) {
            updatePromotionAnalyticsRealTime(
                    request.getPromotionId(),
                    request.getOrderAmount(),
                    request.getDiscountApplied()
            );
        }

        log.info("Usage recorded successfully: logId={}", savedLog.getId());

        return savedLog;
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignPerformanceResponse getCampaignPerformance(Long campaignId, LocalDate date) {
        log.info("Fetching campaign performance: campaignId={}, date={}", campaignId, date);

        Optional<CampaignAnalytics> analyticsOpt = campaignAnalyticsRepository
                .findByCampaignIdAndAnalyticsDate(campaignId, date);

        if (analyticsOpt.isEmpty()) {
            // Return empty metrics if no data for this date
            return CampaignPerformanceResponse.builder()
                    .campaignId(campaignId)
                    .analyticsDate(date)
                    .totalOrders(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .totalDiscount(BigDecimal.ZERO)
                    .uniqueUsers(0)
                    .impressions(0)
                    .clicks(0)
                    .conversionRate(BigDecimal.ZERO)
                    .clickThroughRate(BigDecimal.ZERO)
                    .avgOrderValue(BigDecimal.ZERO)
                    .avgDiscount(BigDecimal.ZERO)
                    .discountPercentage(BigDecimal.ZERO)
                    .build();
        }

        return analyticsMapper.toCampaignPerformanceResponse(analyticsOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignPerformanceResponse> getCampaignPerformanceRange(
            Long campaignId, LocalDate startDate, LocalDate endDate) {

        log.info("Fetching campaign performance range: campaignId={}, start={}, end={}",
                campaignId, startDate, endDate);

        List<CampaignAnalytics> analyticsList = campaignAnalyticsRepository
                .findByCampaignIdAndAnalyticsDateBetween(campaignId, startDate, endDate);

        return analyticsList.stream()
                .map(analyticsMapper::toCampaignPerformanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordCampaignImpression(Long campaignId) {
        log.debug("Recording campaign impression: campaignId={}", campaignId);

        LocalDate today = LocalDate.now();
        CampaignAnalytics analytics = getOrCreateCampaignAnalytics(campaignId, today);
        
        analytics.recordImpression();
        campaignAnalyticsRepository.save(analytics);
    }

    @Override
    @Transactional
    public void recordCampaignClick(Long campaignId) {
        log.debug("Recording campaign click: campaignId={}", campaignId);

        LocalDate today = LocalDate.now();
        CampaignAnalytics analytics = getOrCreateCampaignAnalytics(campaignId, today);
        
        analytics.recordClick();
        campaignAnalyticsRepository.save(analytics);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionMetricsResponse getPromotionMetrics(Long promotionId, LocalDate date) {
        log.info("Fetching promotion metrics: promotionId={}, date={}", promotionId, date);

        Optional<PromotionAnalytics> analyticsOpt = promotionAnalyticsRepository
                .findByPromotionIdAndAnalyticsDate(promotionId, date);

        if (analyticsOpt.isEmpty()) {
            return PromotionMetricsResponse.builder()
                    .promotionId(promotionId)
                    .analyticsDate(date)
                    .usageCount(0)
                    .uniqueUsers(0)
                    .totalDiscount(BigDecimal.ZERO)
                    .totalRevenue(BigDecimal.ZERO)
                    .netRevenue(BigDecimal.ZERO)
                    .avgOrderValue(BigDecimal.ZERO)
                    .roi(BigDecimal.ZERO)
                    .discountPercentage(BigDecimal.ZERO)
                    .build();
        }

        return analyticsMapper.toPromotionMetricsResponse(analyticsOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionMetricsResponse> getPromotionMetricsRange(
            Long promotionId, LocalDate startDate, LocalDate endDate) {

        log.info("Fetching promotion metrics range: promotionId={}, start={}, end={}",
                promotionId, startDate, endDate);

        List<PromotionAnalytics> analyticsList = promotionAnalyticsRepository
                .findByPromotionIdAndAnalyticsDateBetween(promotionId, startDate, endDate);

        return analyticsList.stream()
                .map(analyticsMapper::toPromotionMetricsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(LocalDate date) {
        log.info("Fetching dashboard summary for date: {}", date);

        // Get counts (would normally call Campaign/Promotion services)
        Integer activeCampaigns = 0;  // Placeholder
        Integer activePromotions = 0;  // Placeholder

        // Get today's metrics
        Integer todayOrders = campaignAnalyticsRepository.getTotalOrdersByDate(date);
        BigDecimal todayRevenue = campaignAnalyticsRepository.getTotalRevenueByDate(date);
        BigDecimal todayDiscount = promotionAnalyticsRepository.getTotalDiscountByDate(date);
        BigDecimal avgConversionRate = campaignAnalyticsRepository.getAvgConversionRate(date);
        BigDecimal avgROI = promotionAnalyticsRepository.getAvgROI(date);

        // Get top performers
        List<CampaignAnalytics> topCampaigns = campaignAnalyticsRepository
                .findTopPerformersByRevenue(date);
        List<PromotionAnalytics> topPromotions = promotionAnalyticsRepository
                .findTopPerformersByRevenue(date);

        DashboardSummaryResponse.TopPerformer topCampaign = null;
        if (!topCampaigns.isEmpty()) {
            CampaignAnalytics top = topCampaigns.get(0);
            topCampaign = DashboardSummaryResponse.TopPerformer.builder()
                    .id(top.getCampaignId())
                    .name("Campaign #" + top.getCampaignId())
                    .revenue(top.getTotalRevenue())
                    .usageCount(top.getTotalOrders())
                    .build();
        }

        DashboardSummaryResponse.TopPerformer topPromotion = null;
        if (!topPromotions.isEmpty()) {
            PromotionAnalytics top = topPromotions.get(0);
            topPromotion = DashboardSummaryResponse.TopPerformer.builder()
                    .id(top.getPromotionId())
                    .name("Promotion #" + top.getPromotionId())
                    .revenue(top.getTotalRevenue())
                    .usageCount(top.getUsageCount())
                    .build();
        }

        return DashboardSummaryResponse.builder()
                .activeCampaigns(activeCampaigns)
                .activePromotions(activePromotions)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .todayDiscount(todayDiscount)
                .avgConversionRate(avgConversionRate)
                .avgROI(avgROI)
                .topCampaign(topCampaign)
                .topPromotion(topPromotion)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignPerformanceResponse> getTopCampaigns(LocalDate date, int limit) {
        log.info("Fetching top {} campaigns for date: {}", limit, date);

        List<CampaignAnalytics> topCampaigns = campaignAnalyticsRepository
                .findTopPerformersByRevenue(date);

        return topCampaigns.stream()
                .limit(limit)
                .map(analyticsMapper::toCampaignPerformanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionMetricsResponse> getTopPromotions(LocalDate date, int limit) {
        log.info("Fetching top {} promotions for date: {}", limit, date);

        List<PromotionAnalytics> topPromotions = promotionAnalyticsRepository
                .findTopPerformersByRevenue(date);

        return topPromotions.stream()
                .limit(limit)
                .map(analyticsMapper::toPromotionMetricsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void aggregateDailyAnalytics(LocalDate date) {
        log.info("Running daily aggregation for date: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get all usage logs for the day
        List<PromotionUsageLog> logs = usageLogRepository
                .findByCreatedAtBetween(startOfDay, endOfDay);

        log.info("Found {} usage logs to aggregate", logs.size());

        // Group by promotion ID
        Map<Long, List<PromotionUsageLog>> logsByPromotion = logs.stream()
                .filter(PromotionUsageLog::getSuccess)
                .collect(Collectors.groupingBy(PromotionUsageLog::getPromotionId));

        // Aggregate each promotion
        for (Map.Entry<Long, List<PromotionUsageLog>> entry : logsByPromotion.entrySet()) {
            aggregatePromotionAnalytics(entry.getKey(), date);
        }

        log.info("Daily aggregation completed for {} promotions", logsByPromotion.size());
    }

    @Override
    @Transactional
    public void aggregateCampaignAnalytics(Long campaignId, LocalDate date) {
        log.info("Aggregating campaign analytics: campaignId={}, date={}", campaignId, date);

        // In production, you would aggregate from multiple sources:
        // - Order data
        // - Click tracking
        // - Impression tracking
        // For now, we create/update the record

        CampaignAnalytics analytics = getOrCreateCampaignAnalytics(campaignId, date);
        
        // Calculations already done by business methods
        analytics.calculateAverages();
        analytics.calculateConversionRate();

        campaignAnalyticsRepository.save(analytics);

        log.info("Campaign analytics aggregated successfully");
    }

    @Override
    @Transactional
    public void aggregatePromotionAnalytics(Long promotionId, LocalDate date) {
        log.info("Aggregating promotion analytics: promotionId={}, date={}", promotionId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get usage logs for this promotion and date
        List<PromotionUsageLog> logs = usageLogRepository
                .findByPromotionAndDateRange(promotionId, startOfDay, endOfDay);

        if (logs.isEmpty()) {
            log.info("No usage logs found for promotion {} on {}", promotionId, date);
            return;
        }

        // Calculate metrics
        int usageCount = logs.size();
        
        Integer uniqueUsers = usageLogRepository
                .countUniqueUsersByPromotionAndDateRange(promotionId, startOfDay, endOfDay);

        BigDecimal totalDiscount = logs.stream()
                .map(PromotionUsageLog::getDiscountApplied)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = logs.stream()
                .map(PromotionUsageLog::getOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get or create analytics record
        PromotionAnalytics analytics = promotionAnalyticsRepository
                .findByPromotionIdAndAnalyticsDate(promotionId, date)
                .orElse(PromotionAnalytics.builder()
                        .promotionId(promotionId)
                        .analyticsDate(date)
                        .build());

        // Update metrics
        analytics.setUsageCount(usageCount);
        analytics.setUniqueUsers(uniqueUsers);
        analytics.setTotalDiscount(totalDiscount);
        analytics.setTotalRevenue(totalRevenue);
        analytics.calculateAverageOrderValue();
        analytics.calculateROI();

        promotionAnalyticsRepository.save(analytics);

        log.info("Promotion analytics aggregated: usageCount={}, revenue={}, roi={}",
                usageCount, totalRevenue, analytics.getRoi());
    }

    // Helper methods

    private CampaignAnalytics getOrCreateCampaignAnalytics(Long campaignId, LocalDate date) {
        return campaignAnalyticsRepository
                .findByCampaignIdAndAnalyticsDate(campaignId, date)
                .orElse(CampaignAnalytics.builder()
                        .campaignId(campaignId)
                        .analyticsDate(date)
                        .totalOrders(0)
                        .totalRevenue(BigDecimal.ZERO)
                        .totalDiscount(BigDecimal.ZERO)
                        .uniqueUsers(0)
                        .impressions(0)
                        .clicks(0)
                        .build());
    }

    private void updatePromotionAnalyticsRealTime(
            Long promotionId, BigDecimal orderAmount, BigDecimal discountAmount) {

        LocalDate today = LocalDate.now();
        
        PromotionAnalytics analytics = promotionAnalyticsRepository
                .findByPromotionIdAndAnalyticsDate(promotionId, today)
                .orElse(PromotionAnalytics.builder()
                        .promotionId(promotionId)
                        .analyticsDate(today)
                        .build());

        analytics.incrementMetrics(orderAmount, discountAmount);
        promotionAnalyticsRepository.save(analytics);

        log.debug("Real-time analytics updated for promotion: {}", promotionId);
    }
}
