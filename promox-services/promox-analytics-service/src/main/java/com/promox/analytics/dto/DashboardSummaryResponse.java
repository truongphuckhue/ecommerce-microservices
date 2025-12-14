package com.promox.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private Integer activeCampaigns;
    private Integer activePromotions;
    private Integer todayOrders;
    private BigDecimal todayRevenue;
    private BigDecimal todayDiscount;
    private BigDecimal avgConversionRate;
    private BigDecimal avgROI;
    private TopPerformer topCampaign;
    private TopPerformer topPromotion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopPerformer {
        private Long id;
        private String name;
        private BigDecimal revenue;
        private Integer usageCount;
    }
}
