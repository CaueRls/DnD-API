package com.example.dungeons_dragons_api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    // Armazena um Bucket por IP — ConcurrentHashMap é thread-safe
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Limite: 10 requisições por minuto por IP
    private static final int LIMITE_REQUISICOES = 10;
    private static final Duration PERIODO = Duration.ofMinutes(1);

    public Bucket resolveBucket(String ip) {
        // Se o IP já tem um bucket, retorna ele — senão cria um novo
        return buckets.computeIfAbsent(ip, this::novoBucket);
    }

    private Bucket novoBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(LIMITE_REQUISICOES)
                .refillGreedy(LIMITE_REQUISICOES, PERIODO)
                .build();
        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    public int getLimiteRequisicoes() {
        return LIMITE_REQUISICOES;
    }
}