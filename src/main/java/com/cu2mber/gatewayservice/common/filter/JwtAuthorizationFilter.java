package com.cu2mber.gatewayservice.common.filter;

import com.cu2mber.gatewayservice.common.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Spring Cloud Gateway에서 JWT 기반 인증을 처리하는 Authorization 필터입니다.
 *
 * <p>
 * 이 필터는 모든 Gateway 요청에 대해 실행되며,
 * 화이트리스트로 지정된 경로를 제외한 요청에 대해
 * HTTP Authorization 헤더의 Bearer 토큰을 검증합니다.
 * </p>
 *
 * <p>
 * 토큰이 없거나, 형식이 올바르지 않거나, 검증에 실패할 경우
 * HTTP 401(Unauthorized) 응답을 반환하고 요청을 차단합니다.
 * </p>
 *
 * <p>
 * 본 필터는 GatewayFilter 인터페이스를 구현하여
 * 라우트 단위 또는 글로벌 필터로 적용될 수 있습니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements GatewayFilter {

    /**
     * JWT 토큰의 유효성 검증 및 파싱 로직을 제공하는 Provider 클래스입니다.
     *
     * <p>
     * 토큰 서명 검증, 만료 시간 확인 등의 책임을 가지며,
     * 검증 실패 시 예외를 발생시킵니다.
     * </p>
     */
    private final JwtProvider jwtProvider;

    /**
     * JWT 인증 없이 접근을 허용할 API 경로 목록입니다.
     *
     * <p>
     * 로그인, 회원가입, 토큰 발급/재발급과 같이
     * 인증 이전 단계에서 호출되는 엔드포인트를 포함합니다.
     * </p>
     */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/issue",
            "/auth/refresh",
            "/member/signup",
            "/member/signin"
    );

    /**
     * Gateway 요청을 가로채 JWT 인증을 수행합니다.
     *
     * <p>
     * 처리 흐름은 다음과 같습니다:
     * <ol>
     *     <li>요청 URI 경로가 화이트리스트에 포함되는지 확인</li>
     *     <li>화이트리스트 경로인 경우 인증 없이 다음 필터로 전달</li>
     *     <li>Authorization 헤더에서 Bearer 토큰 추출</li>
     *     <li>JwtProvider를 이용해 토큰 유효성 검증</li>
     *     <li>검증 실패 시 401 Unauthorized 응답 반환</li>
     * </ol>
     * </p>
     *
     * @param exchange 현재 HTTP 요청과 응답 정보를 담고 있는 ServerWebExchange
     * @param chain    Gateway 필터 체인
     * @return Mono<Void> 요청 처리 완료를 나타내는 리액티브 타입
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 인증 없이 허용할 경로는 바로 통과
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // "Bearer " 접두사를 제거한 실제 토큰 값
        String token = authHeader.substring(7);

        try {
            // JWT 유효성 검증
            jwtProvider.validateToken(token);
        } catch (Exception e) {
            // 토큰 검증 실패 시 요청 차단
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 인증 성공 시 다음 필터 또는 라우트로 요청 전달
        return chain.filter(exchange);
    }

    /**
     * 요청 경로가 인증 없이 접근 가능한 화이트리스트 경로인지 확인합니다.
     *
     * @param path 요청 URI 경로
     * @return 화이트리스트에 포함된 경우 true, 그렇지 않으면 false
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
}