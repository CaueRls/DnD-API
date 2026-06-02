package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private static final List<String> ROTAS_PUBLICAS = List.of(
            "/",
            "/index.html",
            "/favicon.ico",
            "/api-keys",
            "/swagger-ui",
            "/v3/api-docs",
            "/h2-console",
            "/css",
            "/js",
            "/assets",
            "/images"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean rotaPublica = ROTAS_PUBLICAS.stream()
                .anyMatch(path::startsWith);

        if (rotaPublica) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Header X-API-Key é obrigatório."
                    }
                    """);
            return;
        }

        boolean valida = apiKeyRepository
                .findByKeyValueAndActiveTrue(apiKey)
                .isPresent();

        if (!valida) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "status": 403,
                      "error": "Forbidden",
                      "message": "API Key inválida ou inativa."
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}