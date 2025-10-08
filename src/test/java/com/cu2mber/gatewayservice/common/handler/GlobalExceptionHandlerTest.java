package com.cu2mber.gatewayservice.common.handler;

import com.cu2mber.gatewayservice.common.exception.CommonHttpException;
import com.cu2mber.gatewayservice.common.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    // 테스트를 위한 더미 라우터 설정
    @TestConfiguration
    static class TestRouterConfig {
        // GlobalExceptionHandler는 @Component이므로 WebFluxTest가 자동으로 스캔하여 빈으로 등록합니다.
        // 따라서 여기에 별도로 빈 등록할 필요는 없습니다.

        @Bean
        public RouterFunction<ServerResponse> testRoutes() {
            return route(GET("/test/common-exception"), request -> {
                throw new CommonHttpException(HttpStatus.BAD_REQUEST.value(), "Test CommonHttpException");
            })
                    .andRoute(GET("/test/unauthorized-exception"), request -> {
                        throw new UnauthorizedException("Test UnauthorizedException");
                    })
                    .andRoute(GET("/test/not-found-exception"), request -> {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test Not Found Exception");
                    })
                    .andRoute(GET("/test/general-exception"), request -> {
                        throw new RuntimeException("Test General Exception");
                    });
        }
    }

    @Test
    void handleCommonHttpException() {
        webTestClient.get().uri("/test/common-exception")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKey("timestamp");
                    assertThat(LocalDateTime.parse(body.get("timestamp").toString()).truncatedTo(ChronoUnit.MINUTES))
                            .isEqualToIgnoringSeconds(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                    assertThat(body.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(body.get("error")).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
                    assertThat(body.get("message")).isEqualTo("Test CommonHttpException");
                    assertThat(body.get("path")).isEqualTo("/test/common-exception");
                });
    }

    @Test
    void handleUnauthorizedException() {
        webTestClient.get().uri("/test/unauthorized-exception")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKey("timestamp");
                    assertThat(LocalDateTime.parse(body.get("timestamp").toString()).truncatedTo(ChronoUnit.MINUTES))
                            .isEqualToIgnoringSeconds(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                    assertThat(body.get("status")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                    assertThat(body.get("error")).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                    assertThat(body.get("message")).isEqualTo("Test UnauthorizedException");
                    assertThat(body.get("path")).isEqualTo("/test/unauthorized-exception");
                });
    }

    @Test
    void handleResponseStatusException() {
        webTestClient.get().uri("/test/not-found-exception")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKey("timestamp");
                    assertThat(LocalDateTime.parse(body.get("timestamp").toString()).truncatedTo(ChronoUnit.MINUTES))
                            .isEqualToIgnoringSeconds(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                    assertThat(body.get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(body.get("error")).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
                    assertThat(body.get("message")).isEqualTo("Test Not Found Exception");
                    assertThat(body.get("path")).isEqualTo("/test/not-found-exception");
                });
    }

    @Test
    void handleGeneralException() {
        webTestClient.get().uri("/test/general-exception")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKey("timestamp");
                    assertThat(LocalDateTime.parse(body.get("timestamp").toString()).truncatedTo(ChronoUnit.MINUTES))
                            .isEqualToIgnoringSeconds(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                    assertThat(body.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    assertThat(body.get("error")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    // 메시지는 "An unexpected error occurred: Test General Exception" 또는 유사한 형태가 될 수 있습니다.
                    assertThat(body.get("message").toString()).contains("서버 내부 오류가 발생했습니다.");
                    assertThat(body.get("path")).isEqualTo("/test/general-exception");
                });
    }
}