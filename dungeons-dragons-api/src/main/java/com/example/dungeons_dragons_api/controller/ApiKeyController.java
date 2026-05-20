package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.ApiKey;
import com.example.dungeons_dragons_api.repository.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Keys", description = "Endpoints para geração e gerenciamento de chaves de API")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository repository;

    @Operation(summary = "Lista todas as API Keys",
            description = "Retorna todas as chaves cadastradas, ativas e inativas.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ApiKey>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    @Operation(summary = "Gera uma nova API Key",
            description = "Cria uma nova chave de API para o owner informado. A chave é gerada automaticamente via UUID.")
    @ApiResponse(responseCode = "201", description = "Chave criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Owner não informado")
    @PostMapping
    public ResponseEntity<?> gerarChave(@RequestParam String owner) {
        if (owner == null || owner.isBlank()) {
            return ResponseEntity.badRequest().body("O campo 'owner' é obrigatório.");
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setOwner(owner);
        apiKey.setKeyValue(UUID.randomUUID().toString()); // gera chave única

        ApiKey saved = repository.save(apiKey);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "Desativa uma API Key",
            description = "Desativa a chave pelo ID. A chave não é deletada, apenas marcada como inativa.")
    @ApiResponse(responseCode = "200", description = "Chave desativada com sucesso")
    @ApiResponse(responseCode = "404", description = "Chave não encontrada")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<?> desativarChave(@PathVariable Long id) {
        return repository.findById(id)
                .map(key -> {
                    key.setActive(false);
                    repository.save(key);
                    return ResponseEntity.ok("Chave desativada com sucesso.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}