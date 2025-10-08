package com.cu2mber.gatewayservice.common.exception;

import lombok.Getter;

/**
 * HTTP 상태 코드와 메시지를 함께 전달할 수 있는 공통 런타임 예외 클래스입니다.
 * <p>
 * 주로 컨트롤러나 서비스에서 HTTP 오류를 표현할 때 사용하며,
 * 상태 코드와 메시지를 함께 포함하여 클라이언트에게 전달할 수 있습니다.
 */
@Getter
public class CommonHttpException extends RuntimeException{

    /** HTTP 상태 코드 */
    private final int statusCode;

    /**
     * 상태 코드와 메시지를 지정하여 예외를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드 (예: 404, 500 등)
     * @param message    예외 메시지
     */
    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 상태 코드, 메시지와 원인(Throwable)을 지정하여 예외를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    예외 메시지
     * @param cause      예외의 원인 Throwable
     */
    public CommonHttpException(final int statusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
