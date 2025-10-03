package com.cu2mber.gatewayservice.common.config;

import com.cu2mber.gatewayservice.common.cache.ServiceCache;
import com.cu2mber.gatewayservice.common.filter.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Gateway의 동적 라우팅 설정을 담당하는 구성 클래스입니다.
 * <p>
 * Eureka(DiscoveryClient)에서 등록된 서비스 리스트를 기반으로 동적 라우트를 생성하며,
 * 각 라우트에 JWT 인증 필터를 적용합니다.
 */
@Configuration
@RequiredArgsConstructor
public class RouterConfig {

    /** JWT 인증 필터 */
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    /** Eureka에서 가져온 서비스 리스트 캐시 */
    private final ServiceCache serviceCache;

    /**
     * Eureka에서 조회한 서비스 리스트를 기반으로 동적 라우트를 생성합니다.
     * <p>
     * 각 서비스에 대해 "/api/{서비스명}/**" 경로로 들어오는 요청을 처리하며,
     * JWT 인증 필터를 적용하고, 경로 프리픽스 2단계를 제거(stripPrefix(2)) 후
     * 서비스명으로 로드밸런싱(lb://{서비스명})합니다.
     *
     * @param builder RouteLocatorBuilder 인스턴스
     * @return 구성된 RouteLocator
     */
    @Bean
    public RouteLocator dynamicRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        // Eureka에서 가져온 서비스 리스트 기반 동적 라우트
        serviceCache.getServices().forEach(serviceName -> {
            routes.route(serviceName, r -> r
                    .path("/api/" + serviceName.toLowerCase() + "/**")
                    .filters(f -> f.stripPrefix(2).filter(jwtAuthorizationFilter))
                    .uri("lb://" + serviceName)
            );
        });

        return routes.build();
    }
}
