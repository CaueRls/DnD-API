package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        // Tenta consumir 1 token do bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Adiciona headers informativos em todas as respostas
        response.addHeader("X-RateLimit-Limit",
                String.valueOf(rateLimitConfig.getLimiteRequisicoes()));
        response.addHeader("X-RateLimit-Remaining",
                String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            // Ainda tem tokens — deixa a requisição passar
            filterChain.doFilter(request, response);
        } else {
            // Sem tokens — bloqueia com 429
            long esperarEmSegundos = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("Retry-After", String.valueOf(esperarEmSegundos));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Limite de requisições excedido. Tente novamente em %d segundos."
                }
                """.formatted(esperarEmSegundos));
        }
    }
}