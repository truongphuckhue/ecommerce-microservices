package com.ecommerce.order.saga;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.kafka.InventoryReservationRequest;
import com.ecommerce.order.kafka.PaymentRequest;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ORDER SAGA ORCHESTRATOR
 * 
 * Manages distributed transaction across multiple services:
 * 1. Reserve inventory
 * 2. Process payment
 * 3. Confirm order
 * 
 * If any step fails, compensating transactions are executed:
 * - Release inventory reservation
 * - Refund payment
 * 
 * This is the ORCHESTRATION-BASED SAGA pattern (not choreography)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INVENTORY_RESERVATION_TOPIC = "inventory-reservation-requests";
    private static final String PAYMENT_PROCESSING_TOPIC = "payment-processing-requests";
    private static final String INVENTORY_RELEASE_TOPIC = "inventory-release-requests";
    private static final String PAYMENT_REFUND_TOPIC = "payment-refund-requests";

    /**
     * Start the saga when order is created
     */
    @Transactional
    public void startSaga(Order order) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting saga {} for order {}", sagaId, order.getOrderNumber());

        order.setSagaId(sagaId);
        order.setSagaStatus(Order.SagaStatus.STARTED);
        orderRepository.save(order);

        // Step 1: Reserve inventory
        reserveInventory(order);
    }

    /**
     * Step 1: Reserve inventory for all order items
     */
    private void reserveInventory(Order order) {
        log.info("[SAGA {}] Step 1: Reserving inventory for order {}", 
                order.getSagaId(), order.getOrderNumber());

        order.getItems().forEach(item -> {
            InventoryReservationRequest request = InventoryReservationRequest.builder()
                    .sagaId(order.getSagaId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build();

            kafkaTemplate.send(INVENTORY_RESERVATION_TOPIC, 
                    item.getProductId().toString(), request);
        });
    }

    /**
     * Handle inventory reservation success
     * Triggered by Kafka event from Inventory Service
     */
    @Transactional
    public void onInventoryReserved(String sagaId, String orderNumber) {
        log.info("[SAGA {}] Inventory reserved successfully for order {}", sagaId, orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        order.setSagaStatus(Order.SagaStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        // Step 2: Process payment
        processPayment(order);
    }

    /**
     * Step 2: Process payment
     */
    private void processPayment(Order order) {
        log.info("[SAGA {}] Step 2: Processing payment for order {}", 
                order.getSagaId(), order.getOrderNumber());

        PaymentRequest request = PaymentRequest.builder()
                .sagaId(order.getSagaId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .build();

        kafkaTemplate.send(PAYMENT_PROCESSING_TOPIC, 
                order.getOrderNumber(), request);
    }

    /**
     * Handle payment success
     * Triggered by Kafka event from Payment Service
     */
    @Transactional
    public void onPaymentProcessed(String sagaId, String orderNumber, String paymentId) {
        log.info("[SAGA {}] Payment processed successfully for order {}, paymentId: {}", 
                sagaId, orderNumber, paymentId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        order.setPaymentId(paymentId);
        order.setSagaStatus(Order.SagaStatus.PAYMENT_PROCESSED);
        orderRepository.save(order);

        // Step 3: Complete order
        completeSaga(order);
    }

    /**
     * Step 3: Complete saga
     */
    private void completeSaga(Order order) {
        log.info("[SAGA {}] Completing saga for order {}", 
                order.getSagaId(), order.getOrderNumber());

        order.setSagaStatus(Order.SagaStatus.COMPLETED);
        order.confirm();
        orderRepository.save(order);

        log.info("[SAGA {}] Saga completed successfully for order {}", 
                order.getSagaId(), order.getOrderNumber());
    }

    /**
     * Handle inventory reservation failure
     * Start compensating transaction
     */
    @Transactional
    public void onInventoryReservationFailed(String sagaId, String orderNumber, String reason) {
        log.error("[SAGA {}] Inventory reservation failed for order {}: {}", 
                sagaId, orderNumber, reason);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        order.setSagaStatus(Order.SagaStatus.FAILED);
        order.fail("Inventory reservation failed: " + reason);
        orderRepository.save(order);

        log.info("[SAGA {}] Saga failed, no compensations needed (first step)", sagaId);
    }

    /**
     * Handle payment failure
     * Compensate by releasing inventory
     */
    @Transactional
    public void onPaymentFailed(String sagaId, String orderNumber, String reason) {
        log.error("[SAGA {}] Payment failed for order {}: {}", 
                sagaId, orderNumber, reason);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        order.setSagaStatus(Order.SagaStatus.COMPENSATING);
        orderRepository.save(order);

        // Compensate: Release inventory reservation
        compensateInventory(order);

        order.setSagaStatus(Order.SagaStatus.COMPENSATED);
        order.fail("Payment failed: " + reason);
        orderRepository.save(order);

        log.info("[SAGA {}] Saga compensated successfully", sagaId);
    }

    /**
     * Compensating transaction: Release inventory
     */
    private void compensateInventory(Order order) {
        log.info("[SAGA {}] COMPENSATING: Releasing inventory for order {}", 
                order.getSagaId(), order.getOrderNumber());

        order.getItems().forEach(item -> {
            InventoryReservationRequest request = InventoryReservationRequest.builder()
                    .sagaId(order.getSagaId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build();

            kafkaTemplate.send(INVENTORY_RELEASE_TOPIC, 
                    item.getProductId().toString(), request);
        });
    }

    /**
     * User cancellation - different from saga failure
     */
    @Transactional
    public void cancelOrder(Order order, String reason) {
        log.info("[SAGA {}] User cancelling order {}", order.getSagaId(), order.getOrderNumber());

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current state");
        }

        // Release inventory if reserved
        if (order.getSagaStatus() == Order.SagaStatus.INVENTORY_RESERVED ||
            order.getSagaStatus() == Order.SagaStatus.PAYMENT_PROCESSED ||
            order.getSagaStatus() == Order.SagaStatus.COMPLETED) {
            compensateInventory(order);
        }

        // Refund payment if processed
        if (order.getSagaStatus() == Order.SagaStatus.PAYMENT_PROCESSED ||
            order.getSagaStatus() == Order.SagaStatus.COMPLETED) {
            compensatePayment(order);
        }

        order.cancel(reason);
        orderRepository.save(order);

        log.info("[SAGA {}] Order cancelled successfully", order.getSagaId());
    }

    /**
     * Compensating transaction: Refund payment
     */
    private void compensatePayment(Order order) {
        log.info("[SAGA {}] COMPENSATING: Refunding payment for order {}", 
                order.getSagaId(), order.getOrderNumber());

        PaymentRequest request = PaymentRequest.builder()
                .sagaId(order.getSagaId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .build();

        kafkaTemplate.send(PAYMENT_REFUND_TOPIC, 
                order.getOrderNumber(), request);
    }
}
