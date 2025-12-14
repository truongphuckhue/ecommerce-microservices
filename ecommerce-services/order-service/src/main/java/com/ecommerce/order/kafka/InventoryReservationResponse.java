package com.ecommerce.order.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response sent from Inventory Service back to Order Service
 * after attempting to reserve stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationResponse implements Serializable {
    
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
     * Product ID
     */
    private Long productId;

    /**
     * Quantity requested
     */
    private Integer quantity;

    /**
     * Success or failure
     */
    private boolean success;

    /**
     * Failure reason (if success = false)
     */
    private String reason;

    /**
     * Reservation ID (if success = true)
     */
    private String reservationId;

    /**
     * Response timestamp
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
}
