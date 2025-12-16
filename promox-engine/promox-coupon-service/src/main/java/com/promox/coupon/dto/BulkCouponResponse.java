package com.promox.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCouponResponse {

    private String batchId;
    private Integer totalGenerated;
    private List<String> couponCodes;
    private String message;
}
