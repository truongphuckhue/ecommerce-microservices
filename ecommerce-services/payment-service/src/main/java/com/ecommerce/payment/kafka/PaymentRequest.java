package com.ecommerce.payment.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Payment request received from Order Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "USD";
    
    private String paymentMethod;
    private String description;
    
    @Builder.Default
    private Long requestTimestamp = System.currentTimeMillis();
}
