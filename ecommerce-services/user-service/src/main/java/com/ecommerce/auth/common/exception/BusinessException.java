package com.ecommerce.auth.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String message;
    private final Object data;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.data = null;
    }

    public BusinessException(String errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
        this.data = null;
    }

    public BusinessException(String errorCode, String message, Object data, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }
}