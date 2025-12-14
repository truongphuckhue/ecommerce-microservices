package com.ecommerce.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayRequest {
    
    private Long userId;
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "USD";
    
    private String paymentMethod;
    private String description;
    
    // Card details (in production, use tokenized card data)
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    
    // Metadata
    private String orderNumber;
    private String customerEmail;
}
