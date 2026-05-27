package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.ApiKey;
import com.example.dungeons_dragons_api.repository.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Keys", description = "Endpoints públicos para geração e gerenciamento de chaves de API")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository repository;

    @Operation(
            summary = "Lista todas as API Keys",
            description = "Retorna todas as chaves cadastradas no banco H2, ativas e inativas. Use para copiar o campo keyValue e autorizar o Swagger.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<List<ApiKey>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    @Operation(
            summary = "Busca uma API Key pelo ID",
            description = "Retorna uma chave específica pelo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Chave encontrada"),
                    @ApiResponse(responseCode = "404", description = "Chave não encontrada")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiKey> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Gera uma nova API Key",
            description = "Cria uma nova chave de API para o owner informado. Este endpoint é público e não exige X-API-Key nem X-Idempotency-Key.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Chave criada com sucesso",
                            content = @Content(
                                    schema = @Schema(implementation = ApiKey.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "id": 1,
                                              "keyValue": "123e4567-e89b-12d3-a456-426614174000",
                                              "owner": "caue",
                                              "active": true,
                                              "createdAt": "2026-05-26T19:30:00"
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Owner não informado")
            }
    )
    @PostMapping
    public ResponseEntity<?> gerarChave(
            @Parameter(description = "Nome do dono da chave", example = "caue", required = true)
            @RequestParam @NotBlank String owner) {

        if (owner == null || owner.isBlank()) {
            return ResponseEntity.badRequest().body("O campo 'owner' é obrigatório.");
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setOwner(owner.trim());
        apiKey.setKeyValue(UUID.randomUUID().toString());
        apiKey.setActive(true);

        ApiKey saved = repository.save(apiKey);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Desativa uma API Key",
            description = "Marca a chave como inativa. A chave continua existindo no banco H2, mas não poderá autenticar requisições.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Chave desativada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Chave não encontrada")
            }
    )
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

    @Operation(
            summary = "Deleta uma API Key do banco H2",
            description = "Remove permanentemente a chave pelo ID. Depois disso ela não poderá mais ser usada. Útil para testes com H2.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Chave deletada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Chave não encontrada")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarChave(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
