package com.promox.flashsale.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "flash_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal flashPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "savings_amount", precision = 10, scale = 2)
    private BigDecimal savingsAmount;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FlashSaleStatus status = FlashSaleStatus.SCHEDULED;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isActive() {
        return status == FlashSaleStatus.ACTIVE
                && startTime.isBefore(LocalDateTime.now())
                && endTime.isAfter(LocalDateTime.now());
    }

    public boolean canStart() {
        return status == FlashSaleStatus.SCHEDULED
                && startTime.isBefore(LocalDateTime.now());
    }

    public boolean shouldEnd() {
        return status == FlashSaleStatus.ACTIVE
                && endTime.isBefore(LocalDateTime.now());
    }

    public boolean isSoldOut() {
        return soldQuantity >= totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return totalQuantity - soldQuantity - reservedQuantity;
    }

    public Integer getRemainingQuantity() {
        return totalQuantity - soldQuantity;
    }

    public BigDecimal getSoldPercentage() {
        if (totalQuantity == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(soldQuantity)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalQuantity), 2, BigDecimal.ROUND_HALF_UP);
    }

    public void calculateDiscountDetails() {
        if (originalPrice != null && flashPrice != null) {
            savingsAmount = originalPrice.subtract(flashPrice);
            discountPercentage = savingsAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    // Enums
    public enum FlashSaleStatus {
        SCHEDULED,     // Scheduled for future
        ACTIVE,        // Currently running
        SOLD_OUT,      // All items sold
        ENDED,         // Time expired
        CANCELLED      // Cancelled
    }
}
