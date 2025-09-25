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

@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

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