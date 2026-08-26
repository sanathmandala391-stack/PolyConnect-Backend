package com.polyconnect.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.integration.sbtet.SbtetClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SbtetCircularService {

    private static final long CACHE_TTL_SECONDS = 15 * 60; // 15 minutes

    private final SbtetClient sbtetClient;
    private final AtomicReference<CachedCirculars> cache = new AtomicReference<>();

    public SbtetCircularService(SbtetClient sbtetClient) {
        this.sbtetClient = sbtetClient;
    }

    public JsonNode getActiveCirculars() {
        CachedCirculars cached = cache.get();
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return cached.data();
        }
        JsonNode fresh = sbtetClient.getActiveCirculars();
        cache.set(new CachedCirculars(fresh, Instant.now().plusSeconds(CACHE_TTL_SECONDS)));
        return fresh;
    }

    private record CachedCirculars(JsonNode data, Instant expiresAt) {}
}
