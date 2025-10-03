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

@Service
public class ServiceCache {

    private final DiscoveryClient discoveryClient;
    private final long ttlSeconds;
    private Cache<String, List<String>> cache;

    public ServiceCache(DiscoveryClient discoveryClient,
                        @Value("${gateway.cache.ttl-seconds:60}") long ttlSeconds) {
        this.discoveryClient = discoveryClient;
        this.ttlSeconds = ttlSeconds;
    }

    @PostConstruct
    public void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();
    }

    public List<String> getServices() {
        return cache.get("services", k -> discoveryClient.getServices());
    }
}
