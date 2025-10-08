package com.cu2mber.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * GatewayService 애플리케이션 진입점 클래스.
 * <p>
 * Spring Boot 애플리케이션으로, Eureka 등 서비스 디스커버리와 연동하여
 * 마이크로서비스 간 API Gateway 역할을 수행합니다.
 * <p>
 * 주요 기능:
 * <ul>
 *     <li>서비스 디스커버리 등록 및 조회</li>
 *     <li>동적 라우팅 처리</li>
 *     <li>JWT 기반 인증 필터 적용</li>
 *     <li>글로벌 예외 처리</li>
 * </ul>
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayServiceApplication {

    /**
     * 애플리케이션 실행 진입점.
     *
     * @param args 애플리케이션 실행 시 전달되는 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}