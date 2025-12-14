package com.promox.reward.dto;

import com.promox.reward.entity.PointTransaction;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarnPointsRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    @NotNull(message = "Transaction type is required")
    private PointTransaction.TransactionType transactionType;
    
    private String description;
    private String referenceType;
    private String referenceId;
    private Integer expiryDays;
}
