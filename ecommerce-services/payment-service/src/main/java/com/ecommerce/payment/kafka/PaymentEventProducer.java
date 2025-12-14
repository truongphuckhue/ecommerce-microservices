package com.ecommerce.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Sends payment responses back to Order Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_SUCCESS_TOPIC = "payment-processing-success";
    private static final String PAYMENT_FAILURE_TOPIC = "payment-processing-failure";
    private static final String REFUND_SUCCESS_TOPIC = "payment-refund-success";
    private static final String REFUND_FAILURE_TOPIC = "payment-refund-failure";

    /**
     * Send payment success event to Order Service
     */
    public void sendPaymentSuccess(PaymentResponse response) {
        log.info("Sending payment success event: sagaId={}, orderNumber={}, paymentId={}", 
                response.getSagaId(), response.getOrderNumber(), response.getPaymentId());
        
        kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, response.getOrderNumber(), response);
    }

    /**
     * Send payment failure event to Order Service
     */
    public void sendPaymentFailure(PaymentResponse response) {
        log.info("Sending payment failure event: sagaId={}, orderNumber={}, reason={}", 
                response.getSagaId(), response.getOrderNumber(), response.getReason());
        
        kafkaTemplate.send(PAYMENT_FAILURE_TOPIC, response.getOrderNumber(), response);
    }

    /**
     * Send refund success event to Order Service
     */
    public void sendRefundSuccess(PaymentResponse response) {
        log.info("Sending refund success event: sagaId={}, orderNumber={}, paymentId={}", 
                response.getSagaId(), response.getOrderNumber(), response.getPaymentId());
        
        kafkaTemplate.send(REFUND_SUCCESS_TOPIC, response.getOrderNumber(), response);
    }

    /**
     * Send refund failure event to Order Service
     */
    public void sendRefundFailure(PaymentResponse response) {
        log.error("Sending refund failure event: sagaId={}, orderNumber={}, reason={}", 
                response.getSagaId(), response.getOrderNumber(), response.getReason());
        
        kafkaTemplate.send(REFUND_FAILURE_TOPIC, response.getOrderNumber(), response);
    }
}
