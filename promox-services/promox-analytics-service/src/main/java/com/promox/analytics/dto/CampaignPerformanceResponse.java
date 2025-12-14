package com.promox.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignPerformanceResponse {
    private Long campaignId;
    private LocalDate analyticsDate;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;
    private Integer uniqueUsers;
    private Integer impressions;
    private Integer clicks;
    private BigDecimal conversionRate;
    private BigDecimal clickThroughRate;
    private BigDecimal avgOrderValue;
    private BigDecimal avgDiscount;
    private BigDecimal discountPercentage;
}
