package com.example.dungeons_dragons_api.config;

import com.example.dungeons_dragons_api.filter.ApiKeyFilter;
import com.example.dungeons_dragons_api.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ApiKeyFilter apiKeyFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF — não necessário para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // Desabilita sessão — API stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Libera rotas públicas, protege o resto
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/h2-console/**",
                                "/api-keys/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Permite o console H2 em frames
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                // Adiciona nossos filtros antes do filtro padrão do Spring Security
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}