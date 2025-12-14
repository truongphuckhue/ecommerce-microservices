package com.ecommerce.order.kafka;

import com.ecommerce.order.saga.OrderSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to responses from other services (Inventory, Payment)
 * and triggers appropriate saga transitions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventListener {

    private final OrderSaga orderSaga;

    /**
     * Inventory reservation succeeded
     */
    @KafkaListener(
        topics = "inventory-reservation-success",
        groupId = "order-service"
    )
    public void handleInventoryReservationSuccess(InventoryReservationResponse response) {
        log.info("Received inventory reservation success: sagaId={}, orderNumber={}", 
                response.getSagaId(), response.getOrderNumber());
        
        orderSaga.onInventoryReserved(response.getSagaId(), response.getOrderNumber());
    }

    /**
     * Inventory reservation failed
     */
    @KafkaListener(
        topics = "inventory-reservation-failure",
        groupId = "order-service"
    )
    public void handleInventoryReservationFailure(InventoryReservationResponse response) {
        log.error("Received inventory reservation failure: sagaId={}, orderNumber={}, reason={}", 
                response.getSagaId(), response.getOrderNumber(), response.getReason());
        
        orderSaga.onInventoryReservationFailed(
                response.getSagaId(), 
                response.getOrderNumber(), 
                response.getReason()
        );
    }

    /**
     * Payment processing succeeded
     */
    @KafkaListener(
        topics = "payment-processing-success",
        groupId = "order-service"
    )
    public void handlePaymentSuccess(PaymentResponse response) {
        log.info("Received payment success: sagaId={}, orderNumber={}, paymentId={}", 
                response.getSagaId(), response.getOrderNumber(), response.getPaymentId());
        
        orderSaga.onPaymentProcessed(
                response.getSagaId(), 
                response.getOrderNumber(), 
                response.getPaymentId()
        );
    }

    /**
     * Payment processing failed
     */
    @KafkaListener(
        topics = "payment-processing-failure",
        groupId = "order-service"
    )
    public void handlePaymentFailure(PaymentResponse response) {
        log.error("Received payment failure: sagaId={}, orderNumber={}, reason={}", 
                response.getSagaId(), response.getOrderNumber(), response.getReason());
        
        orderSaga.onPaymentFailed(
                response.getSagaId(), 
                response.getOrderNumber(), 
                response.getReason()
        );
    }

    /**
     * Inventory release confirmed (compensation)
     */
    @KafkaListener(
        topics = "inventory-release-success",
        groupId = "order-service"
    )
    public void handleInventoryReleaseSuccess(InventoryReservationResponse response) {
        log.info("Inventory released successfully: sagaId={}, orderNumber={}", 
                response.getSagaId(), response.getOrderNumber());
        // Just logging, no action needed
    }

    /**
     * Payment refund confirmed (compensation)
     */
    @KafkaListener(
        topics = "payment-refund-success",
        groupId = "order-service"
    )
    public void handlePaymentRefundSuccess(PaymentResponse response) {
        log.info("Payment refunded successfully: sagaId={}, orderNumber={}", 
                response.getSagaId(), response.getOrderNumber());
        // Just logging, no action needed
    }
}
