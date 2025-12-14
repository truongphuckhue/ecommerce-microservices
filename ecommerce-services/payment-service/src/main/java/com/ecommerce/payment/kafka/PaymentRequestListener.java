package com.ecommerce.payment.kafka;

import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to payment processing requests from Order Service
 * This is part of the Order Saga
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestListener {

    private final PaymentService paymentService;

    /**
     * Handle payment processing request from Order Service
     * 
     * Topic: payment-processing-requests
     * From: Order Service (OrderSaga)
     * 
     * Flow:
     * 1. Receive payment request
     * 2. Process payment through gateway
     * 3. If success → Send payment-processing-success
     * 4. If failure → Send payment-processing-failure
     */
    @KafkaListener(
        topics = "payment-processing-requests",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentProcessingRequest(PaymentRequest request) {
        log.info("Received payment processing request: sagaId={}, orderNumber={}, amount={}", 
                request.getSagaId(), request.getOrderNumber(), request.getAmount());

        try {
            // Process payment
            paymentService.processPayment(request);
            
            log.info("Payment processed successfully: sagaId={}, orderNumber={}", 
                    request.getSagaId(), request.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Payment processing failed: sagaId={}, orderNumber={}, error={}", 
                    request.getSagaId(), request.getOrderNumber(), e.getMessage(), e);
            
            // Error handling is done in service layer
            // Service will send failure event
        }
    }

    /**
     * Handle payment refund request (compensation)
     * 
     * Topic: payment-refund-requests
     * From: Order Service (OrderSaga - compensation)
     * 
     * Flow:
     * 1. Receive refund request
     * 2. Process refund through gateway
     * 3. If success → Send payment-refund-success
     * 4. If failure → Log error (refund failures need manual intervention)
     */
    @KafkaListener(
        topics = "payment-refund-requests",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRefundRequest(PaymentRequest request) {
        log.info("Received payment refund request: sagaId={}, orderNumber={}, amount={}", 
                request.getSagaId(), request.getOrderNumber(), request.getAmount());

        try {
            // Process refund
            paymentService.refundPayment(request);
            
            log.info("Payment refunded successfully: sagaId={}, orderNumber={}", 
                    request.getSagaId(), request.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Payment refund failed: sagaId={}, orderNumber={}, error={}", 
                    request.getSagaId(), request.getOrderNumber(), e.getMessage(), e);
            
            // Refund failures are critical - may need manual intervention
            // Consider sending alert to ops team
        }
    }
}
