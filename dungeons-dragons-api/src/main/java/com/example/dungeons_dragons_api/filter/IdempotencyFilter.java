package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.model.IdempotencyRecord;
import com.example.dungeons_dragons_api.repository.IdempotencyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    private static final List<String> ROTAS_PUBLICAS_SEM_IDEMPOTENCIA = List.of(
            "/api-keys",
            "/swagger-ui",
            "/v3/api-docs",
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean rotaPublica = ROTAS_PUBLICAS_SEM_IDEMPOTENCIA.stream().anyMatch(path::startsWith);

        if (rotaPublica) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader("X-Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Header X-Idempotency-Key é obrigatório em operações POST. Gere um UUID e envie no header para evitar criação duplicada."
                    }
                    """);
            return;
        }

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            response.setStatus(record.getResponseStatus());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("X-Idempotency-Key", idempotencyKey);
            response.setHeader("X-Idempotent-Replayed", "true");
            response.getWriter().write(record.getResponseBody());
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        int responseStatus = responseWrapper.getStatus();

        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setResponseBody(responseBody);
        record.setResponseStatus(responseStatus);
        idempotencyRepository.save(record);

        responseWrapper.setHeader("X-Idempotency-Key", idempotencyKey);
        responseWrapper.setHeader("X-Idempotent-Replayed", "false");
        responseWrapper.copyBodyToResponse();
    }
}
