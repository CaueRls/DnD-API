package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A chave da API é obrigatória")
    @Size(max = 120, message = "A chave da API deve ter no máximo 120 caracteres")
    @Column(unique = true, nullable = false, length = 120)
    private String keyValue;

    @NotBlank(message = "O dono da chave é obrigatório")
    @Size(min = 2, max = 80, message = "O dono da chave deve ter entre 2 e 80 caracteres")
    @Column(nullable = false, length = 80)
    private String owner;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
