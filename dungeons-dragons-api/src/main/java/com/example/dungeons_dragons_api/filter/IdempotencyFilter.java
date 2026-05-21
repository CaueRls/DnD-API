package com.example.dungeons_dragons_api.filter;

import com.example.dungeons_dragons_api.model.IdempotencyRecord;
import com.example.dungeons_dragons_api.repository.IdempotencyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Aplica somente em requisições POST
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader("X-Idempotency-Key");

        // Se não veio o header, deixa passar normalmente sem idempotência
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verifica se já existe um registro com essa chave
        Optional<IdempotencyRecord> existing =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            // Chave já usada — devolve o resultado anterior sem reprocessar
            IdempotencyRecord record = existing.get();
            response.setStatus(record.getResponseStatus());
            response.setContentType("application/json");
            response.setHeader("X-Idempotency-Key", idempotencyKey);
            response.setHeader("X-Idempotent-Replayed", "true"); // ← avisa que é replay
            response.getWriter().write(record.getResponseBody());
            return;
        }

        // Chave nova — usa wrapper para capturar a resposta antes de enviar
        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        // Processa a requisição normalmente
        filterChain.doFilter(request, responseWrapper);

        // Captura o resultado
        String responseBody = new String(responseWrapper.getContentAsByteArray());
        int responseStatus = responseWrapper.getStatus();

        // Salva no banco para futuras requisições com a mesma chave
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setResponseBody(responseBody);
        record.setResponseStatus(responseStatus);
        idempotencyRepository.save(record);

        // Adiciona o header na resposta
        response.setHeader("X-Idempotency-Key", idempotencyKey);

        // Envia a resposta original para o cliente
        responseWrapper.copyBodyToResponse();
    }
}