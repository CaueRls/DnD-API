package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_idempotency")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A chave de idempotência é obrigatória")
    @Size(max = 120, message = "A chave de idempotência deve ter no máximo 120 caracteres")
    @Column(unique = true, nullable = false, length = 120)
    private String idempotencyKey;

    @NotNull(message = "O corpo da resposta é obrigatório")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String responseBody;

    @Min(value = 100, message = "O status HTTP deve ser maior ou igual a 100")
    @Max(value = 599, message = "O status HTTP deve ser menor ou igual a 599")
    @Column(nullable = false)
    private int responseStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
