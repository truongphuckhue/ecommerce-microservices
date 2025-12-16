package com.promox.promotion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateDiscountResponse {

    private Boolean applicable;
    private String message;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private List<AppliedPromotion> appliedPromotions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedPromotion {
        private Long promotionId;
        private String promotionCode;
        private String promotionName;
        private BigDecimal discountAmount;
        private String discountDescription;
    }
}
