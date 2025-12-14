package com.ecommerce.payment.gateway;

import lombok.Getter;

@Getter
public class PaymentGatewayException extends Exception {
    
    private final String errorCode;
    private final String errorMessage;

    public PaymentGatewayException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public PaymentGatewayException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
