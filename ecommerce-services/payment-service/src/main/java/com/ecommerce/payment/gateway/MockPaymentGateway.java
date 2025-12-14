package com.ecommerce.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock Payment Gateway Implementation
 * Simulates payment processing for testing
 * 
 * In production, replace with real gateway implementations:
 * - StripePaymentGateway
 * - PayPalPaymentGateway
 * - etc.
 */
@Component
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    private final Random random = new Random();
    
    // Simulated success rate: 90%
    private static final double SUCCESS_RATE = 0.9;
    
    // Simulated processing time: 300-800ms
    private static final int MIN_DELAY_MS = 300;
    private static final int MAX_DELAY_MS = 800;

    @Override
    public String charge(PaymentGatewayRequest request) throws PaymentGatewayException {
        log.info("Processing payment: userId={}, amount={}, currency={}", 
                request.getUserId(), request.getAmount(), request.getCurrency());

        try {
            // Simulate network delay
            simulateProcessingDelay();

            // Simulate gateway processing
            boolean success = simulatePaymentResult(request.getAmount());

            if (success) {
                String transactionId = generateTransactionId();
                log.info("Payment successful: transactionId={}", transactionId);
                return transactionId;
            } else {
                String errorCode = generateErrorCode();
                String errorMessage = getErrorMessage(errorCode);
                log.warn("Payment failed: errorCode={}, message={}", errorCode, errorMessage);
                throw new PaymentGatewayException(errorCode, errorMessage);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("GATEWAY_ERROR", "Payment processing interrupted");
        }
    }

    @Override
    public String refund(String originalTransactionId, BigDecimal amount) throws PaymentGatewayException {
        log.info("Processing refund: originalTxId={}, amount={}", originalTransactionId, amount);

        try {
            // Simulate network delay
            simulateProcessingDelay();

            // Refunds have 95% success rate (higher than charges)
            boolean success = random.nextDouble() < 0.95;

            if (success) {
                String refundId = "REF-" + generateTransactionId();
                log.info("Refund successful: refundId={}", refundId);
                return refundId;
            } else {
                throw new PaymentGatewayException("REFUND_FAILED", "Refund processing failed");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("GATEWAY_ERROR", "Refund processing interrupted");
        }
    }

    @Override
    public PaymentGatewayStatus getPaymentStatus(String transactionId) {
        // Mock implementation
        return PaymentGatewayStatus.builder()
                .transactionId(transactionId)
                .status("COMPLETED")
                .responseCode("00")
                .responseMessage("Success")
                .build();
    }

    // Helper methods

    private void simulateProcessingDelay() throws InterruptedException {
        int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
        Thread.sleep(delay);
    }

    private boolean simulatePaymentResult(BigDecimal amount) {
        // Payments > $10000 have higher failure rate (risk checks)
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return random.nextDouble() < 0.7;
        }
        
        // Normal success rate
        return random.nextDouble() < SUCCESS_RATE;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
    }

    private String generateErrorCode() {
        String[] errorCodes = {
            "INSUFFICIENT_FUNDS",
            "CARD_DECLINED",
            "EXPIRED_CARD",
            "INVALID_CARD",
            "PROCESSING_ERROR"
        };
        return errorCodes[random.nextInt(errorCodes.length)];
    }

    private String getErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "INSUFFICIENT_FUNDS" -> "Insufficient funds in account";
            case "CARD_DECLINED" -> "Card was declined by issuing bank";
            case "EXPIRED_CARD" -> "Card has expired";
            case "INVALID_CARD" -> "Invalid card number";
            case "PROCESSING_ERROR" -> "Payment processing error";
            default -> "Payment failed";
        };
    }
}
