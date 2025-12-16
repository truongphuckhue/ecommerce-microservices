package com.promox.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionMetricsResponse {
    private Long promotionId;
    private LocalDate analyticsDate;
    private Integer usageCount;
    private Integer uniqueUsers;
    private BigDecimal totalDiscount;
    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private BigDecimal avgOrderValue;
    private BigDecimal roi;
    private BigDecimal discountPercentage;
}
