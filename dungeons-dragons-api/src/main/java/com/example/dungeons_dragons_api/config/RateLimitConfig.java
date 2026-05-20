package com.example.dungeons_dragons_api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int LIMITE_REQUISICOES = 10;
    private static final Duration PERIODO = Duration.ofMinutes(1);

    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, this::novoBucket);
    }

    private Bucket novoBucket(String ip) {
        // API da versão 8.1.1 — usa Refill.greedy em vez de .refillGreedy()
        Refill refill = Refill.greedy(LIMITE_REQUISICOES, PERIODO);
        Bandwidth limite = Bandwidth.classic(LIMITE_REQUISICOES, refill);
        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    public int getLimiteRequisicoes() {
        return LIMITE_REQUISICOES;
    }
}