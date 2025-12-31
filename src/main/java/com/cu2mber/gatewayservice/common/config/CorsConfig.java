package com.cu2mber.gatewayservice.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Cloud Gateway에서 전역 CORS(Cross-Origin Resource Sharing) 설정을 담당하는 구성 클래스입니다.
 * <p>
 * 이 설정은 브라우저 기반 클라이언트(프론트엔드)에서 발생하는 교차 출처 요청을
 * Gateway 단에서 처리하기 위해 {@link CorsWebFilter}를 등록합니다.
 * <p>
 * Gateway는 WebFlux 기반이므로 Servlet 환경의 {@code CorsFilter}가 아닌
 * {@code CorsWebFilter}를 사용해야 합니다.
 *
 * <h3>주요 설정 내용</h3>
 * <ul>
 *     <li>허용 Origin: front-service 컨테이너의 8090 포트</li>
 *     <li>허용 HTTP Method: GET, POST, PUT, DELETE, PATCH, OPTIONS</li>
 *     <li>허용 Header: Authorization, Content-Type</li>
 *     <li>노출 Header: Authorization</li>
 *     <li>Credentials 사용 여부: 사용하지 않음 (JWT 헤더 기반 인증)</li>
 * </ul>
 *
 * <p>
 * 이 설정을 통해 브라우저의 Preflight(OPTIONS) 요청이 정상적으로 처리되며,
 * JWT 기반 인증 요청이 CORS 정책에 의해 차단되지 않도록 합니다.
 * </p>
 */
@Configuration
public class CorsConfig {

    /**
     * Gateway 전역 CORS 처리를 위한 {@link CorsWebFilter} Bean을 생성합니다.
     * <p>
     * 등록된 CORS 정책은 모든 경로("/**")에 적용되며,
     * Authorization 헤더를 사용하는 JWT 기반 인증 구조에 맞게 구성되어 있습니다.
     *
     * @return 전역 CORS 정책이 적용된 {@link CorsWebFilter} 인스턴스
     */
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 Origin (Docker 네트워크 기준)
        config.setAllowedOrigins(List.of(
                "http://front-service:8090"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 허용할 요청 헤더
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        // 클라이언트에 노출할 응답 헤더
        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        // 쿠키 기반 인증을 사용하지 않으므로 credentials 비활성화
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
