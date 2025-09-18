package com.cu2mber.gatewayservice.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceCache {

    private final DiscoveryClient discoveryClient;
    private Cache<String, List<String>> cache;

    public ServiceCache(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostConstruct
    public void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    public List<String> getServices() {
        return cache.get("services", k -> discoveryClient.getServices());
    }
}
