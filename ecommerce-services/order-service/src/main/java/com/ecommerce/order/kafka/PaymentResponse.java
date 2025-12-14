package com.ecommerce.order.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response sent from Payment Service back to Order Service
 * after attempting to process payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Saga ID to correlate with request
     */
    private String sagaId;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * Order number
     */
    private String orderNumber;

    /**
     * Success or failure
     */
    private boolean success;

    /**
     * Payment ID (if success = true)
     * This is the transaction ID from payment gateway
     */
    private String paymentId;

    /**
     * Amount charged
     */
    private BigDecimal amount;

    /**
     * Failure reason (if success = false)
     */
    private String reason;

    /**
     * Payment status (COMPLETED, FAILED, PENDING, REFUNDED)
     */
    private String status;

    /**
     * Response timestamp
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    /**
     * Additional metadata
     */
    private String metadata;
}
