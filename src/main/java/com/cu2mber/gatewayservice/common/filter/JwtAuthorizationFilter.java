package com.cu2mber.gatewayservice.common.filter;

import com.cu2mber.gatewayservice.common.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT 토큰 기반 인증을 수행하는 Spring Cloud Gateway 필터 클래스입니다.
 * <p>
 * 요청 헤더의 Authorization 값을 확인하고, "Bearer "로 시작하는 JWT 토큰을 추출하여
 * JwtProvider를 통해 검증합니다. 검증 실패 시 HTTP 401(Unauthorized)을 반환합니다.
 * <p>
 * 이 필터는 각 Gateway 라우트에 적용되어 보호된 API 요청을 인증합니다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements GatewayFilter {

    /** JWT 검증 및 토큰 관련 로직 제공 */
    private final JwtProvider jwtProvider;

    /**
     * 요청을 필터링하고 JWT 인증을 수행합니다.
     *
     * @param exchange 현재 HTTP 요청/응답 정보
     * @param chain    다음 필터 체인
     * @return Mono<Void> 필터 체인의 완료 신호
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // JWT 토큰 검증 로직
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);  // "Bearer "제거

        jwtProvider.validateToken(token);

        return chain.filter(exchange);
    }
}
