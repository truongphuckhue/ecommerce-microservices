package com.ecommerce.payment.dto;

import lombok.*;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String status;
    private Double amount;
    private String transactionId;
}
