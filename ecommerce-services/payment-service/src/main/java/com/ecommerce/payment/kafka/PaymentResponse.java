package com.ecommerce.payment.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Payment response sent back to Order Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private String orderNumber;
    private boolean success;
    private String paymentId;
    private BigDecimal amount;
    private String reason;
    private String status;
    
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
    
    private String metadata;
}
