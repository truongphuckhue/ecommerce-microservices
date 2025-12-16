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
public class CalculateDiscountRequest {

    private String promotionCode;
    private List<String> promotionCodes; // For stacking
    private Long userId;
    private BigDecimal orderTotal;
    private List<OrderItem> items;
    private String userSegment;
    private String location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private Long productId;
        private String productName;
        private String category;
        private BigDecimal price;
        private Integer quantity;
    }
}
