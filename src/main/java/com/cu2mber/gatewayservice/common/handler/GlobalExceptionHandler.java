package com.cu2mber.gatewayservice.common.handler;

import com.cu2mber.gatewayservice.common.exception.CommonHttpException;
import com.cu2mber.gatewayservice.common.exception.UnauthorizedException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 처리 핸들러 클래스.
 * <p>
 * Spring WebFlux 환경에서 발생하는 모든 예외를 처리하며,
 * 공통 HTTP 예외(CommonHttpException), 인증 예외(UnauthorizedException),
 * ResponseStatusException, 그 외 내부 서버 오류를 JSON 형태로 반환합니다.
 * <p>
 * 반환 JSON 구조:
 * <pre>
 * {
 *   "timestamp": ISO-8601 형식 시간,
 *   "status": HTTP 상태 코드,
 *   "error": 상태 코드 설명,
 *   "message": 오류 메시지,
 *   "path": 요청 URI
 * }
 * </pre>
 * 로그에는 발생한 예외와 메시지를 기록합니다.
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    /** JSON 직렬화를 위한 ObjectMapper */
    private final ObjectMapper objectMapper;

    /** 기본 서버 내부 오류 메시지 */
    private static final String ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

    /**
     * 모든 예외를 처리하고, 적절한 HTTP 상태 코드와 JSON 응답을 반환합니다.
     *
     * @param exchange 현재 HTTP 요청/응답 정보
     * @param ex       발생한 Throwable 예외
     * @return Mono<Void> 처리 완료 시점 신호
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus httpStatus;
        String errorMessage;
        Map<String, Object> errorAttributes = new HashMap<>();

        if (ex instanceof CommonHttpException) {
            CommonHttpException commonEx = (CommonHttpException) ex;
            httpStatus = HttpStatus.valueOf(commonEx.getStatusCode());
            errorMessage = commonEx.getMessage();
            log.error("요청 처리 중 CommonHttpException 발생: {}", errorMessage);
        } else if (ex instanceof UnauthorizedException) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            errorMessage = ex.getMessage();
            log.error("요청 처리 중 UnauthorizedException 발생: {}", errorMessage);
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusEx = (ResponseStatusException) ex;
            httpStatus = (HttpStatus) responseStatusEx.getStatusCode();
            errorMessage = responseStatusEx.getReason();
            log.error("요청 처리 중 ResponseStatusException 발생: {}", errorMessage);
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = ERROR_MESSAGE;
            log.error("서버 내부에서 처리되지 않은 예외 발생: ", ex);
        }

        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("status", httpStatus.value());
        errorAttributes.put("error", httpStatus.getReasonPhrase());
        errorAttributes.put("message", errorMessage);
        errorAttributes.put("path", exchange.getRequest().getPath().value());

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        return exchange.getResponse().writeWith(
                Mono.defer(() -> {
                    try {
                        byte[] jsonBytes = objectMapper.writeValueAsBytes(errorAttributes);
                        return Mono.just(bufferFactory.wrap(jsonBytes));
                    } catch (JsonProcessingException e) {
                        log.error("에러 응답 JSON 직렬화 실패", e);

                        return Mono.just(bufferFactory.wrap("{\"message\": \"응답 처리 중 오류 발생\"}".getBytes()));
                    }
                })
        );
    }
}