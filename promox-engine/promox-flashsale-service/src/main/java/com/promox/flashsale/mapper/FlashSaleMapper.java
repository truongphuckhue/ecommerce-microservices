package com.promox.flashsale.mapper;

import com.promox.flashsale.dto.FlashSaleRequest;
import com.promox.flashsale.dto.FlashSaleResponse;
import com.promox.flashsale.entity.FlashSale;
import org.springframework.stereotype.Component;

@Component
public class FlashSaleMapper {

    public FlashSale toEntity(FlashSaleRequest request) {
        if (request == null) {
            return null;
        }

        return FlashSale.builder()
                .campaignId(request.getCampaignId())
                .productId(request.getProductId())
                .productName(request.getProductName())
                .productSku(request.getProductSku())
                .originalPrice(request.getOriginalPrice())
                .flashPrice(request.getFlashPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalQuantity(request.getTotalQuantity())
                .soldQuantity(0)
                .reservedQuantity(0)
                .status(FlashSale.FlashSaleStatus.SCHEDULED)
                .perUserLimit(request.getPerUserLimit())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntityFromRequest(FlashSale flashSale, FlashSaleRequest request) {
        if (request.getProductName() != null) {
            flashSale.setProductName(request.getProductName());
        }
        if (request.getProductSku() != null) {
            flashSale.setProductSku(request.getProductSku());
        }
        if (request.getOriginalPrice() != null) {
            flashSale.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getFlashPrice() != null) {
            flashSale.setFlashPrice(request.getFlashPrice());
        }
        if (request.getStartTime() != null) {
            flashSale.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            flashSale.setEndTime(request.getEndTime());
        }
        if (request.getTotalQuantity() != null) {
            flashSale.setTotalQuantity(request.getTotalQuantity());
        }
        if (request.getPerUserLimit() != null) {
            flashSale.setPerUserLimit(request.getPerUserLimit());
        }
        if (request.getMetadata() != null) {
            flashSale.setMetadata(request.getMetadata());
        }
    }

    public FlashSaleResponse toResponse(FlashSale flashSale) {
        if (flashSale == null) {
            return null;
        }

        return FlashSaleResponse.builder()
                .id(flashSale.getId())
                .campaignId(flashSale.getCampaignId())
                .productId(flashSale.getProductId())
                .productName(flashSale.getProductName())
                .productSku(flashSale.getProductSku())
                .originalPrice(flashSale.getOriginalPrice())
                .flashPrice(flashSale.getFlashPrice())
                .discountPercentage(flashSale.getDiscountPercentage())
                .savingsAmount(flashSale.getSavingsAmount())
                .startTime(flashSale.getStartTime())
                .endTime(flashSale.getEndTime())
                .totalQuantity(flashSale.getTotalQuantity())
                .soldQuantity(flashSale.getSoldQuantity())
                .reservedQuantity(flashSale.getReservedQuantity())
                .availableQuantity(flashSale.getAvailableQuantity())
                .remainingQuantity(flashSale.getRemainingQuantity())
                .soldPercentage(flashSale.getSoldPercentage())
                .status(flashSale.getStatus())
                .perUserLimit(flashSale.getPerUserLimit())
                .metadata(flashSale.getMetadata())
                .createdAt(flashSale.getCreatedAt())
                .updatedAt(flashSale.getUpdatedAt())
                .isActive(flashSale.isActive())
                .isSoldOut(flashSale.isSoldOut())
                .canStart(flashSale.canStart())
                .shouldEnd(flashSale.shouldEnd())
                .build();
    }
}
