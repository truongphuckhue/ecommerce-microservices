package com.ecommerce.payment.gateway;

import java.math.BigDecimal;

/**
 * Payment Gateway Interface
 * In production, implement integrations with real gateways:
 * - Stripe
 * - PayPal
 * - Square
 * - Authorize.net
 */
public interface PaymentGateway {
    
    /**
     * Process payment charge
     * 
     * @return Transaction ID from gateway
     * @throws PaymentGatewayException if payment fails
     */
    String charge(PaymentGatewayRequest request) throws PaymentGatewayException;
    
    /**
     * Refund payment
     * 
     * @return Refund transaction ID
     * @throws PaymentGatewayException if refund fails
     */
    String refund(String originalTransactionId, BigDecimal amount) throws PaymentGatewayException;
    
    /**
     * Check payment status
     */
    PaymentGatewayStatus getPaymentStatus(String transactionId);
}
