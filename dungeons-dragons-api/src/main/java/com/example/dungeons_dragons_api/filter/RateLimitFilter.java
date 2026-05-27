package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    private static final List<String> ROTAS_PUBLICAS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(request.getMethod())
                || ROTAS_PUBLICAS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getLimiteRequisicoes()));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long esperarEmSegundos = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
        response.addHeader("Retry-After", String.valueOf(esperarEmSegundos));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
            {
              "status": 429,
              "error": "Too Many Requests",
              "message": "Limite de requisições excedido. Tente novamente em %d segundos."
            }
            """.formatted(esperarEmSegundos));
    }
}
