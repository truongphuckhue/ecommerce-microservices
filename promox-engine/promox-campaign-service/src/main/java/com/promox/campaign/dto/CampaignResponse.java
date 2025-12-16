package com.promox.campaign.dto;

import com.promox.campaign.entity.Campaign;
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
public class CampaignResponse {

    private Long id;
    private Long merchantId;
    private String name;
    private String description;
    private Campaign.CampaignType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Campaign.CampaignStatus status;
    private BigDecimal budget;
    private BigDecimal spentAmount;
    private BigDecimal remainingBudget;
    private BigDecimal budgetUsagePercentage;
    private Integer priority;
    private String targetAudience;
    private String targetSegmentIds;
    private String metadata;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper flags
    private Boolean isActive;
    private Boolean isExpired;
}
