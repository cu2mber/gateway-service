package com.cu2mber.gatewayservice.exception;

public class UnauthorizedException extends CommonHttpException{

    private static final int HTTP_STATUS_CODE = 401;

    public UnauthorizedException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}