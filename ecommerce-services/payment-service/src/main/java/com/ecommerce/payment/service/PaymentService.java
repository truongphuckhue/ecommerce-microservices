package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.kafka.PaymentRequest;

public interface PaymentService {
    
    /**
     * Process payment from saga request
     */
    void processPayment(PaymentRequest request);
    
    /**
     * Refund payment (compensation)
     */
    void refundPayment(PaymentRequest request);
    
    /**
     * Get payment by order number
     */
    Payment getPaymentByOrderNumber(String orderNumber);
}
