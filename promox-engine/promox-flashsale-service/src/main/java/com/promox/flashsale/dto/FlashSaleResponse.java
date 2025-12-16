package com.promox.flashsale.dto;

import com.promox.flashsale.entity.FlashSale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleResponse {

    private Long id;
    private Long campaignId;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal originalPrice;
    private BigDecimal flashPrice;
    private BigDecimal discountPercentage;
    private BigDecimal savingsAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalQuantity;
    private Integer soldQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer remainingQuantity;
    private BigDecimal soldPercentage;
    private FlashSale.FlashSaleStatus status;
    private Integer perUserLimit;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper flags
    private Boolean isActive;
    private Boolean isSoldOut;
    private Boolean canStart;
    private Boolean shouldEnd;
}
