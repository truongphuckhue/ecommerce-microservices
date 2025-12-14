package com.promox.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CampaignType type;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "spent_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "target_audience", length = 50)
    private String targetAudience;

    @Column(name = "target_segment_ids", columnDefinition = "jsonb")
    private String targetSegmentIds;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE 
            && startDate.isBefore(LocalDateTime.now())
            && endDate.isAfter(LocalDateTime.now());
    }

    public boolean canBeActivated() {
        return status == CampaignStatus.SCHEDULED 
            && startDate.isBefore(LocalDateTime.now());
    }

    public boolean isExpired() {
        return endDate.isBefore(LocalDateTime.now());
    }

    public BigDecimal getRemainingBudget() {
        if (budget == null) return null;
        return budget.subtract(spentAmount);
    }

    public BigDecimal getBudgetUsagePercentage() {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(budget, 2, BigDecimal.ROUND_HALF_UP);
    }

    // Enums
    public enum CampaignType {
        SEASONAL,      // Black Friday, Christmas, etc.
        FLASH,         // Flash sales
        LOYALTY,       // Loyalty program campaigns
        CLEARANCE,     // Clearance sales
        NEW_LAUNCH     // New product launch
    }

    public enum CampaignStatus {
        DRAFT,         // Being created
        SCHEDULED,     // Scheduled for future
        ACTIVE,        // Currently running
        PAUSED,        // Temporarily paused
        ENDED,         // Completed
        CANCELLED      // Cancelled
    }
}
