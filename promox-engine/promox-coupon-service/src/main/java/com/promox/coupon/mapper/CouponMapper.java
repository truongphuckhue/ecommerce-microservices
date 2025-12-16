package com.promox.coupon.mapper;

import com.promox.coupon.dto.CouponRequest;
import com.promox.coupon.dto.CouponResponse;
import com.promox.coupon.entity.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public Coupon toEntity(CouponRequest request) {
        if (request == null) {
            return null;
        }

        return Coupon.builder()
                .code(request.getCode().toUpperCase())
                .campaignId(request.getCampaignId())
                .couponType(request.getCouponType())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .status(Coupon.CouponStatus.ACTIVE)
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .perUserLimit(request.getPerUserLimit())
                .assignedUserId(request.getAssignedUserId())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntityFromRequest(Coupon coupon, CouponRequest request) {
        if (request.getCode() != null) {
            coupon.setCode(request.getCode().toUpperCase());
        }
        if (request.getCouponType() != null) {
            coupon.setCouponType(request.getCouponType());
        }
        if (request.getDiscountType() != null) {
            coupon.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            coupon.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getMinOrderValue() != null) {
            coupon.setMinOrderValue(request.getMinOrderValue());
        }
        if (request.getValidFrom() != null) {
            coupon.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            coupon.setValidTo(request.getValidTo());
        }
        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }
        if (request.getPerUserLimit() != null) {
            coupon.setPerUserLimit(request.getPerUserLimit());
        }
        if (request.getAssignedUserId() != null) {
            coupon.setAssignedUserId(request.getAssignedUserId());
        }
        if (request.getMetadata() != null) {
            coupon.setMetadata(request.getMetadata());
        }
    }

    public CouponResponse toResponse(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .campaignId(coupon.getCampaignId())
                .couponType(coupon.getCouponType())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .validFrom(coupon.getValidFrom())
                .validTo(coupon.getValidTo())
                .status(coupon.getStatus())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .remainingUsage(coupon.getRemainingUsage())
                .usagePercentage(coupon.getUsagePercentage())
                .perUserLimit(coupon.getPerUserLimit())
                .assignedUserId(coupon.getAssignedUserId())
                .metadata(coupon.getMetadata())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .createdBy(coupon.getCreatedBy())
                .batchId(coupon.getBatchId())
                .isValid(coupon.isValid())
                .isExpired(coupon.isExpired())
                .isExhausted(coupon.isExhausted())
                .canBeUsed(coupon.canBeUsed())
                .build();
    }
}
