package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.gateway.PaymentGateway;
import com.ecommerce.payment.gateway.PaymentGatewayException;
import com.ecommerce.payment.gateway.PaymentGatewayRequest;
import com.ecommerce.payment.kafka.PaymentEventProducer;
import com.ecommerce.payment.kafka.PaymentRequest;
import com.ecommerce.payment.kafka.PaymentResponse;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer eventProducer;

    /**
     * Process payment from Order Service saga
     * This is called when Kafka event is received
     */
    @Override
    @Transactional
    public void processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}, amount: {}", 
                request.getOrderNumber(), request.getAmount());

        try {
            // 1. Check for idempotency - already processed?
            if (paymentRepository.existsBySagaId(request.getSagaId())) {
                log.warn("Payment already processed for sagaId: {}", request.getSagaId());
                // Send success event anyway (idempotent)
                Payment existing = paymentRepository.findBySagaId(request.getSagaId())
                        .orElseThrow();
                sendSuccessEvent(request, existing);
                return;
            }

            // 2. Create payment record
            Payment payment = createPaymentRecord(request);
            payment = paymentRepository.save(payment);

            // 3. Mark as processing
            payment.markAsProcessing();
            payment = paymentRepository.save(payment);

            // 4. Call payment gateway
            PaymentGatewayRequest gatewayRequest = buildGatewayRequest(request);
            String transactionId = paymentGateway.charge(gatewayRequest);

            // 5. Mark as completed
            payment.markAsCompleted(transactionId);
            payment.setGatewayResponseCode("00");
            payment.setGatewayResponseMessage("Payment successful");
            payment = paymentRepository.save(payment);

            log.info("Payment completed successfully: paymentId={}, transactionId={}", 
                    payment.getPaymentNumber(), transactionId);

            // 6. Send success event to Order Service
            sendSuccessEvent(request, payment);

        } catch (PaymentGatewayException e) {
            log.error("Payment gateway error: sagaId={}, error={}", 
                    request.getSagaId(), e.getMessage());

            // Update payment record with failure
            Payment payment = paymentRepository.findBySagaId(request.getSagaId())
                    .orElse(createPaymentRecord(request));
            payment.markAsFailed(e.getErrorMessage());
            payment.setGatewayResponseCode(e.getErrorCode());
            payment.setGatewayResponseMessage(e.getErrorMessage());
            paymentRepository.save(payment);

            // Send failure event to Order Service
            sendFailureEvent(request, e.getErrorMessage());

        } catch (Exception e) {
            log.error("Unexpected error processing payment: sagaId={}, error={}", 
                    request.getSagaId(), e.getMessage(), e);

            // Send failure event
            sendFailureEvent(request, "Internal payment processing error");
        }
    }

    /**
     * Refund payment (compensation transaction)
     * This is called when order is cancelled
     */
    @Override
    @Transactional
    public void refundPayment(PaymentRequest request) {
        log.info("Processing refund for order: {}, amount: {}", 
                request.getOrderNumber(), request.getAmount());

        try {
            // 1. Find original payment
            Payment payment = paymentRepository.findByOrderNumber(request.getOrderNumber())
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found for order: " + request.getOrderNumber()));

            // 2. Check if refundable
            if (!payment.canBeRefunded()) {
                log.error("Payment cannot be refunded: status={}", payment.getStatus());
                sendRefundFailureEvent(request, "Payment cannot be refunded in status: " + payment.getStatus());
                return;
            }

            // 3. Check amount
            BigDecimal refundAmount = request.getAmount() != null 
                    ? request.getAmount() 
                    : payment.getRemainingRefundableAmount();

            if (refundAmount.compareTo(payment.getRemainingRefundableAmount()) > 0) {
                log.error("Refund amount exceeds refundable amount");
                sendRefundFailureEvent(request, "Refund amount exceeds available amount");
                return;
            }

            // 4. Process refund through gateway
            String refundTransactionId = paymentGateway.refund(
                    payment.getGatewayTransactionId(), 
                    refundAmount
            );

            // 5. Update payment record
            payment.markAsRefunded(refundAmount, refundTransactionId);
            paymentRepository.save(payment);

            log.info("Refund completed successfully: paymentId={}, refundTxId={}", 
                    payment.getPaymentNumber(), refundTransactionId);

            // 6. Send success event to Order Service
            sendRefundSuccessEvent(request, payment);

        } catch (PaymentGatewayException e) {
            log.error("Refund gateway error: sagaId={}, error={}", 
                    request.getSagaId(), e.getMessage());
            sendRefundFailureEvent(request, e.getErrorMessage());

        } catch (Exception e) {
            log.error("Unexpected error processing refund: sagaId={}, error={}", 
                    request.getSagaId(), e.getMessage(), e);
            sendRefundFailureEvent(request, "Internal refund processing error");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderNumber));
    }

    // Helper methods

    private Payment createPaymentRecord(PaymentRequest request) {
        return Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD) // Default
                .sagaId(request.getSagaId())
                .build();
    }

    private String generatePaymentNumber() {
        String date = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + date + "-" + uuid;
    }

    private PaymentGatewayRequest buildGatewayRequest(PaymentRequest request) {
        return PaymentGatewayRequest.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .description("Payment for order: " + request.getOrderNumber())
                .orderNumber(request.getOrderNumber())
                .build();
    }

    private void sendSuccessEvent(PaymentRequest request, Payment payment) {
        PaymentResponse response = PaymentResponse.builder()
                .sagaId(request.getSagaId())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .success(true)
                .paymentId(payment.getPaymentNumber())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();

        eventProducer.sendPaymentSuccess(response);
    }

    private void sendFailureEvent(PaymentRequest request, String reason) {
        PaymentResponse response = PaymentResponse.builder()
                .sagaId(request.getSagaId())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .success(false)
                .reason(reason)
                .status("FAILED")
                .build();

        eventProducer.sendPaymentFailure(response);
    }

    private void sendRefundSuccessEvent(PaymentRequest request, Payment payment) {
        PaymentResponse response = PaymentResponse.builder()
                .sagaId(request.getSagaId())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .success(true)
                .paymentId(payment.getPaymentNumber())
                .amount(payment.getRefundedAmount())
                .status(payment.getStatus().name())
                .build();

        eventProducer.sendRefundSuccess(response);
    }

    private void sendRefundFailureEvent(PaymentRequest request, String reason) {
        PaymentResponse response = PaymentResponse.builder()
                .sagaId(request.getSagaId())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .success(false)
                .reason(reason)
                .status("REFUND_FAILED")
                .build();

        eventProducer.sendRefundFailure(response);
    }
}
