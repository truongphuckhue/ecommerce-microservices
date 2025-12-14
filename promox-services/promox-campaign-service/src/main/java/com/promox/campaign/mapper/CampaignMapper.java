package com.promox.campaign.mapper;

import com.promox.campaign.dto.CampaignRequest;
import com.promox.campaign.dto.CampaignResponse;
import com.promox.campaign.entity.Campaign;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CampaignMapper {

    public Campaign toEntity(CampaignRequest request) {
        if (request == null) {
            return null;
        }

        return Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .targetAudience(request.getTargetAudience())
                .targetSegmentIds(request.getTargetSegmentIds())
                .metadata(request.getMetadata())
                .merchantId(request.getMerchantId())
                .status(Campaign.CampaignStatus.DRAFT)
                .spentAmount(BigDecimal.ZERO)
                .build();
    }

    public void updateEntityFromRequest(Campaign campaign, CampaignRequest request) {
        if (request.getName() != null) {
            campaign.setName(request.getName());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            campaign.setType(request.getType());
        }
        if (request.getStartDate() != null) {
            campaign.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
        }
        if (request.getBudget() != null) {
            campaign.setBudget(request.getBudget());
        }
        if (request.getPriority() != null) {
            campaign.setPriority(request.getPriority());
        }
        if (request.getTargetAudience() != null) {
            campaign.setTargetAudience(request.getTargetAudience());
        }
        if (request.getTargetSegmentIds() != null) {
            campaign.setTargetSegmentIds(request.getTargetSegmentIds());
        }
        if (request.getMetadata() != null) {
            campaign.setMetadata(request.getMetadata());
        }
    }

    public CampaignResponse toResponse(Campaign campaign) {
        if (campaign == null) {
            return null;
        }

        return CampaignResponse.builder()
                .id(campaign.getId())
                .merchantId(campaign.getMerchantId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .type(campaign.getType())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .budget(campaign.getBudget())
                .spentAmount(campaign.getSpentAmount())
                .remainingBudget(campaign.getRemainingBudget())
                .budgetUsagePercentage(campaign.getBudgetUsagePercentage())
                .priority(campaign.getPriority())
                .targetAudience(campaign.getTargetAudience())
                .targetSegmentIds(campaign.getTargetSegmentIds())
                .metadata(campaign.getMetadata())
                .createdBy(campaign.getCreatedBy())
                .updatedBy(campaign.getUpdatedBy())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .isActive(campaign.isActive())
                .isExpired(campaign.isExpired())
                .build();
    }
}
