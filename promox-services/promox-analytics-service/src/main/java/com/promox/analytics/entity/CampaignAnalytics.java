package com.promox.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_analytics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"campaign_id", "analytics_date"}),
       indexes = {
           @Index(name = "idx_campaign_analytics_campaign", columnList = "campaign_id"),
           @Index(name = "idx_campaign_analytics_date", columnList = "analytics_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CampaignAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    // Order Metrics
    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_discount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "unique_users", nullable = false)
    @Builder.Default
    private Integer uniqueUsers = 0;

    // Conversion Metrics
    @Column(name = "impressions", nullable = false)
    @Builder.Default
    private Integer impressions = 0;

    @Column(name = "clicks", nullable = false)
    @Builder.Default
    private Integer clicks = 0;

    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate;

    // Average Values
    @Column(name = "avg_order_value", precision = 10, scale = 2)
    private BigDecimal avgOrderValue;

    @Column(name = "avg_discount", precision = 10, scale = 2)
    private BigDecimal avgDiscount;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public void calculateAverages() {
        if (totalOrders > 0) {
            this.avgOrderValue = totalRevenue.divide(
                BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP
            );
            this.avgDiscount = totalDiscount.divide(
                BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP
            );
        } else {
            this.avgOrderValue = BigDecimal.ZERO;
            this.avgDiscount = BigDecimal.ZERO;
        }
    }

    public void calculateConversionRate() {
        if (impressions > 0) {
            this.conversionRate = BigDecimal.valueOf(totalOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(impressions), 2, RoundingMode.HALF_UP);
        } else {
            this.conversionRate = BigDecimal.ZERO;
        }
    }

    public BigDecimal getClickThroughRate() {
        if (impressions > 0) {
            return BigDecimal.valueOf(clicks)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(impressions), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getDiscountPercentage() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            return totalDiscount
                .multiply(BigDecimal.valueOf(100))
                .divide(totalRevenue, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public void incrementMetrics(BigDecimal orderAmount, BigDecimal discountAmount) {
        this.totalOrders += 1;
        this.totalRevenue = this.totalRevenue.add(orderAmount);
        this.totalDiscount = this.totalDiscount.add(discountAmount);
        calculateAverages();
    }

    public void recordImpression() {
        this.impressions += 1;
        calculateConversionRate();
    }

    public void recordClick() {
        this.clicks += 1;
    }
}
