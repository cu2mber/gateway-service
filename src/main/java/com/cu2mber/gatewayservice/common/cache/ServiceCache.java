package com.cu2mber.gatewayservice.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 서비스 이름 목록을 캐싱하여 Eureka(DiscoveryClient) 조회를 최소화하는 캐시 서비스 클래스입니다.
 * <p>
 * 서비스 리스트는 TTL(Time-To-Live) 기반으로 갱신되며, 지정된 시간 이후 캐시가 만료되면
 * 다음 조회 시 DiscoveryClient에서 새로운 데이터를 가져옵니다.
 */
@Service
public class ServiceCache {

    /** Eureka DiscoveryClient */
    private final DiscoveryClient discoveryClient;

    /** 캐시 만료 시간(초 단위) */
    private final long ttlSeconds;

    /** 서비스 이름 목록을 저장하는 Caffeine 캐시 */
    private Cache<String, List<String>> cache;

    /**
     * ServiceCache 생성자
     *
     * @param discoveryClient Eureka에서 서비스 목록을 조회하기 위한 DiscoveryClient
     * @param ttlSeconds      캐시의 만료 시간(초 단위), properties에서 주입 가능
     */
    public ServiceCache(DiscoveryClient discoveryClient,
                        @Value("${gateway.cache.ttl-seconds:60}") long ttlSeconds) {
        this.discoveryClient = discoveryClient;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * PostConstruct 초기화 메서드.
     * <p>
     * Caffeine 캐시를 TTL 기반으로 초기화합니다.
     */
    @PostConstruct
    public void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 서비스 이름 목록을 조회합니다.
     * <p>
     * 캐시에 존재하지 않으면 DiscoveryClient에서 가져와 캐시에 저장하고 반환합니다.
     *
     * @return Eureka에 등록된 서비스 이름 목록
     */
    public List<String> getServices() {
        return cache.get("services", k -> discoveryClient.getServices());
    }
}
