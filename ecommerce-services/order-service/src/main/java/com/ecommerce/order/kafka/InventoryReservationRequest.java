package com.ecommerce.order.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request sent from Order Service to Inventory Service
 * to reserve stock for an order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationRequest implements Serializable {
    
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
     * Product to reserve
     */
    private Long productId;

    /**
     * Quantity to reserve
     */
    private Integer quantity;

    /**
     * Timestamp for timeout tracking
     */
    private Long timestamp;

    @Builder.Default
    private Long requestTimestamp = System.currentTimeMillis();
}
