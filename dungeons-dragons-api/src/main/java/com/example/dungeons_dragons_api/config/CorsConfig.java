package com.example.dungeons_dragons_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origens permitidas — quem pode consumir a API
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",   // React
            "http://localhost:4200",   // Angular
            "http://localhost:8080"    // própria API (Swagger)
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Headers que o cliente pode enviar
        config.setAllowedHeaders(List.of(
            "Content-Type",
            "Authorization",
            "X-API-Key",
            "X-API-Version",
            "X-Idempotency-Key"
        ));

        // Headers que o cliente pode ler na resposta
        config.setExposedHeaders(List.of(
            "X-API-Version",
            "X-Idempotency-Key",
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "Retry-After"
        ));

        // Permite envio de cookies/credenciais
        config.setAllowCredentials(true);

        // Tempo em segundos que o browser cacheia a config de CORS
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Aplica esta configuração para todas as rotas
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}