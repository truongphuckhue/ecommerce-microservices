package com.ecommerce.order.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request sent from Order Service to Payment Service
 * to process payment for an order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Saga ID to track this distributed transaction
     */
    private String sagaId;

    /**
     * Order ID from Order Service
     */
    private Long orderId;

    /**
     * Order number (human-readable)
     */
    private String orderNumber;

    /**
     * User ID (customer)
     */
    private Long userId;

    /**
     * Amount to charge
     */
    private BigDecimal amount;

    /**
     * Currency (default USD)
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment method (optional)
     */
    private String paymentMethod;

    /**
     * Description
     */
    private String description;

    /**
     * Timestamp for timeout tracking
     */
    @Builder.Default
    private Long requestTimestamp = System.currentTimeMillis();
}
