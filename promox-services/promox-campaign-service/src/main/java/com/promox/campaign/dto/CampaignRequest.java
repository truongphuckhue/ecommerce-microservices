package com.promox.campaign.dto;

import com.promox.campaign.entity.Campaign;
import jakarta.validation.constraints.*;
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
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(min = 3, max = 255, message = "Campaign name must be between 3 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Campaign type is required")
    private Campaign.CampaignType type;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Budget must have maximum 10 digits and 2 decimal places")
    private BigDecimal budget;

    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority;

    private String targetAudience;

    private String targetSegmentIds;

    private String metadata;

    private Long merchantId;

    // Validation method
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle this
        }
        return endDate.isAfter(startDate);
    }
}
