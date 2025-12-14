package com.promox.analytics.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordUsageRequest {
    @NotNull(message = "Promotion ID is required")
    private Long promotionId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String orderId;
    
    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.0", message = "Discount must be positive")
    private BigDecimal discountApplied;
    
    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01", message = "Order amount must be positive")
    private BigDecimal orderAmount;
    
    @NotNull(message = "Success status is required")
    private Boolean success;
    
    private String failureReason;
    private String ipAddress;
}
