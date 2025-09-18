package com.cu2mber.gatewayservice.common.exception;

public class UnauthorizedException extends CommonHttpException{

    private static final int HTTP_STATUS_CODE = 401;

    public UnauthorizedException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}