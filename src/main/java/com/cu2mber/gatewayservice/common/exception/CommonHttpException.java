package com.cu2mber.gatewayservice.common.exception;

import lombok.Getter;

@Getter
public class CommonHttpException extends RuntimeException{

    private final int statusCode;

    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public CommonHttpException(final int statusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
