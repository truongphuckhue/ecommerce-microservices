package com.promox.flashsale.dto;

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
public class FlashSaleRequest {

    private Long campaignId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String productName;

    @Size(max = 100, message = "Product SKU cannot exceed 100 characters")
    private String productSku;

    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    private BigDecimal originalPrice;

    @NotNull(message = "Flash price is required")
    @DecimalMin(value = "0.01", message = "Flash price must be greater than 0")
    private BigDecimal flashPrice;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Total quantity must be at least 1")
    private Integer totalQuantity;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    private String metadata;

    @AssertTrue(message = "Flash price must be less than original price")
    public boolean isFlashPriceLower() {
        if (originalPrice == null || flashPrice == null) {
            return true;
        }
        return flashPrice.compareTo(originalPrice) < 0;
    }

    @AssertTrue(message = "End time must be after start time")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
