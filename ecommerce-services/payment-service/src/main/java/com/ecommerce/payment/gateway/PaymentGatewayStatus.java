package com.ecommerce.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayStatus {
    
    private String transactionId;
    private String status;
    private String responseCode;
    private String responseMessage;
}
