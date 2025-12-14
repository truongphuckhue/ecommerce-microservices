package com.promox.flashsale.dto;

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
public class PurchaseResponse {

    private Boolean success;
    private String message;
    private Long purchaseId;
    private Long flashSaleId;
    private Long userId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal savings;
    private Integer remainingStock;
    private LocalDateTime purchasedAt;
}
