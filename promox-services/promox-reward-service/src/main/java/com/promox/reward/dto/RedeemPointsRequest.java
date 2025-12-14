package com.promox.reward.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemPointsRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    private String description;
    private String referenceType;
    private String referenceId;
}
