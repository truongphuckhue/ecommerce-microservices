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
@Table(name = "promotion_analytics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "analytics_date"}),
       indexes = {
           @Index(name = "idx_promotion_analytics_promotion", columnList = "promotion_id"),
           @Index(name = "idx_promotion_analytics_date", columnList = "analytics_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PromotionAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    // Usage Metrics
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "unique_users", nullable = false)
    @Builder.Default
    private Integer uniqueUsers = 0;

    // Financial Metrics
    @Column(name = "total_discount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "total_revenue", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "avg_order_value", precision = 10, scale = 2)
    private BigDecimal avgOrderValue;

    // ROI (Return on Investment)
    @Column(name = "roi", precision = 10, scale = 2)
    private BigDecimal roi;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public void calculateAverageOrderValue() {
        if (usageCount > 0) {
            this.avgOrderValue = totalRevenue.divide(
                BigDecimal.valueOf(usageCount), 2, RoundingMode.HALF_UP
            );
        } else {
            this.avgOrderValue = BigDecimal.ZERO;
        }
    }

    public void calculateROI() {
        // ROI = ((Revenue - Discount) / Discount) * 100
        // Shows how much profit per dollar of discount
        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal netRevenue = totalRevenue.subtract(totalDiscount);
            this.roi = netRevenue
                .multiply(BigDecimal.valueOf(100))
                .divide(totalDiscount, 2, RoundingMode.HALF_UP);
        } else {
            this.roi = BigDecimal.ZERO;
        }
    }

    public BigDecimal getDiscountPercentage() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            return totalDiscount
                .multiply(BigDecimal.valueOf(100))
                .divide(totalRevenue, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getNetRevenue() {
        return totalRevenue.subtract(totalDiscount);
    }

    public void incrementMetrics(BigDecimal orderAmount, BigDecimal discountAmount) {
        this.usageCount += 1;
        this.totalRevenue = this.totalRevenue.add(orderAmount);
        this.totalDiscount = this.totalDiscount.add(discountAmount);
        calculateAverageOrderValue();
        calculateROI();
    }
}
