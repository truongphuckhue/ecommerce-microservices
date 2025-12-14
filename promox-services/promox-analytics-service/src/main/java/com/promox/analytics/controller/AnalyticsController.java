package com.promox.analytics.controller;

import com.promox.analytics.dto.*;
import com.promox.analytics.entity.PromotionUsageLog;
import com.promox.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics Management", description = "APIs for analytics, reporting, and metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/usage")
    @Operation(summary = "Record promotion usage", description = "Log promotion usage for analytics")
    public ResponseEntity<ApiResponse<PromotionUsageLog>> recordUsage(
            @Valid @RequestBody RecordUsageRequest request) {

        log.info("REST request to record usage: promotionId={}", request.getPromotionId());

        PromotionUsageLog usageLog = analyticsService.recordUsage(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usage recorded successfully", usageLog));
    }

    @GetMapping("/campaigns/{campaignId}/performance")
    @Operation(summary = "Get campaign performance", description = "Get performance metrics for a campaign on specific date")
    public ResponseEntity<ApiResponse<CampaignPerformanceResponse>> getCampaignPerformance(
            @PathVariable Long campaignId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get campaign performance: campaignId={}, date={}", campaignId, date);

        if (date == null) {
            date = LocalDate.now();
        }

        CampaignPerformanceResponse response = analyticsService.getCampaignPerformance(campaignId, date);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/campaigns/{campaignId}/performance/range")
    @Operation(summary = "Get campaign performance range", description = "Get performance metrics for date range")
    public ResponseEntity<ApiResponse<List<CampaignPerformanceResponse>>> getCampaignPerformanceRange(
            @PathVariable Long campaignId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get campaign performance range: campaignId={}, start={}, end={}",
                campaignId, startDate, endDate);

        List<CampaignPerformanceResponse> response = analyticsService
                .getCampaignPerformanceRange(campaignId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/campaigns/{campaignId}/impression")
    @Operation(summary = "Record impression", description = "Record campaign impression for analytics")
    public ResponseEntity<ApiResponse<Void>> recordImpression(@PathVariable Long campaignId) {
        log.debug("REST request to record impression: campaignId={}", campaignId);

        analyticsService.recordCampaignImpression(campaignId);

        return ResponseEntity.ok(ApiResponse.success("Impression recorded", null));
    }

    @PostMapping("/campaigns/{campaignId}/click")
    @Operation(summary = "Record click", description = "Record campaign click for analytics")
    public ResponseEntity<ApiResponse<Void>> recordClick(@PathVariable Long campaignId) {
        log.debug("REST request to record click: campaignId={}", campaignId);

        analyticsService.recordCampaignClick(campaignId);

        return ResponseEntity.ok(ApiResponse.success("Click recorded", null));
    }

    @GetMapping("/promotions/{promotionId}/metrics")
    @Operation(summary = "Get promotion metrics", description = "Get metrics for a promotion on specific date")
    public ResponseEntity<ApiResponse<PromotionMetricsResponse>> getPromotionMetrics(
            @PathVariable Long promotionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get promotion metrics: promotionId={}, date={}", promotionId, date);

        if (date == null) {
            date = LocalDate.now();
        }

        PromotionMetricsResponse response = analyticsService.getPromotionMetrics(promotionId, date);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/promotions/{promotionId}/metrics/range")
    @Operation(summary = "Get promotion metrics range", description = "Get metrics for date range")
    public ResponseEntity<ApiResponse<List<PromotionMetricsResponse>>> getPromotionMetricsRange(
            @PathVariable Long promotionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get promotion metrics range: promotionId={}, start={}, end={}",
                promotionId, startDate, endDate);

        List<PromotionMetricsResponse> response = analyticsService
                .getPromotionMetricsRange(promotionId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Get dashboard summary", description = "Get overall dashboard summary for specific date")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get dashboard summary: date={}", date);

        if (date == null) {
            date = LocalDate.now();
        }

        DashboardSummaryResponse response = analyticsService.getDashboardSummary(date);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/top-campaigns")
    @Operation(summary = "Get top campaigns", description = "Get top performing campaigns")
    public ResponseEntity<ApiResponse<List<CampaignPerformanceResponse>>> getTopCampaigns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("REST request to get top campaigns: date={}, limit={}", date, limit);

        if (date == null) {
            date = LocalDate.now();
        }

        List<CampaignPerformanceResponse> response = analyticsService.getTopCampaigns(date, limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/top-promotions")
    @Operation(summary = "Get top promotions", description = "Get top performing promotions")
    public ResponseEntity<ApiResponse<List<PromotionMetricsResponse>>> getTopPromotions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("REST request to get top promotions: date={}, limit={}", date, limit);

        if (date == null) {
            date = LocalDate.now();
        }

        List<PromotionMetricsResponse> response = analyticsService.getTopPromotions(date, limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/aggregate/daily")
    @Operation(summary = "Trigger daily aggregation", description = "Manually trigger daily analytics aggregation")
    public ResponseEntity<ApiResponse<Void>> aggregateDailyAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to aggregate daily analytics: date={}", date);

        if (date == null) {
            date = LocalDate.now().minusDays(1); // Yesterday by default
        }

        analyticsService.aggregateDailyAnalytics(date);

        return ResponseEntity.ok(ApiResponse.success("Daily aggregation completed", null));
    }
}
