package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    // Rotas que NÃO precisam de API Key
    private static final List<String> ROTAS_PUBLICAS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/h2-console",
            "/api-keys"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Verifica se é rota pública — se for, deixa passar sem validar
        boolean isPublica = ROTAS_PUBLICAS.stream()
                .anyMatch(path::startsWith);

        if (isPublica) {
            filterChain.doFilter(request, response);
            return;
        }

        // Busca o header X-API-Key
        String apiKey = request.getHeader("X-API-Key");

        // Header ausente
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

        // Valida a chave no banco
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

        // Chave válida — deixa passar
        filterChain.doFilter(request, response);
    }
}