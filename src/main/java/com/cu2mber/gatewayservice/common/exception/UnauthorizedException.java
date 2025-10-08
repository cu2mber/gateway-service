package com.cu2mber.gatewayservice.common.exception;

/**
 * 인증되지 않은 사용자 접근 시 발생하는 예외 클래스입니다.
 * <p>
 * HTTP 상태 코드 401(Unauthorized)과 함께 메시지를 전달하며,
 * 주로 인증 실패나 토큰 검증 실패 시 사용됩니다.
 * <p>
 * CommonHttpException을 상속하여 HTTP 상태 코드와 메시지를 함께 제공.
 */
public class UnauthorizedException extends CommonHttpException{

    /** 고정된 HTTP 상태 코드 401 */
    private static final int HTTP_STATUS_CODE = 401;

    /**
     * 예외 메시지를 지정하여 UnauthorizedException을 생성합니다.
     *
     * @param message 예외 메시지
     */
    public UnauthorizedException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}