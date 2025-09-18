package com.cu2mber.gatewayservice.common.config;

import com.cu2mber.gatewayservice.common.cache.ServiceCache;
import com.cu2mber.gatewayservice.common.filter.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouterConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final ServiceCache serviceCache;

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
