package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.exception.ResourceNotFoundException;
import com.example.dungeons_dragons_api.model.ApiKey;
import com.example.dungeons_dragons_api.repository.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Keys", description = "Endpoints para geração e gerenciamento de chaves de API")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository repository;

    @Operation(
            summary = "Lista todas as API Keys",
            description = "Retorna todas as chaves cadastradas, ativas e inativas. Endpoint público para fins acadêmicos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<List<ApiKey>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    @Operation(
            summary = "Gera uma nova API Key",
            description = "Cria uma nova chave de API para o owner informado. A chave é gerada automaticamente via UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Owner não informado"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<ApiKey> gerarChave(
            @RequestParam
            @NotBlank(message = "O campo owner é obrigatório")
            String owner
    ) {
        ApiKey apiKey = new ApiKey();
        apiKey.setOwner(owner);
        apiKey.setKeyValue(UUID.randomUUID().toString());

        ApiKey saved = repository.save(apiKey);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(
            summary = "Desativa uma API Key",
            description = "Marca a chave como inativa. A chave permanece no banco, mas não pode mais autenticar requisições."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<ApiKey> desativarChave(@PathVariable Long id) {
        ApiKey key = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Key não encontrada com o ID: " + id));

        key.setActive(false);
        return ResponseEntity.ok(repository.save(key));
    }

    @Operation(
            summary = "Remove uma API Key",
            description = "Remove definitivamente a chave do banco H2. Depois disso ela não poderá ser usada novamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletarChave(@PathVariable Long id) {
        ApiKey key = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Key não encontrada com o ID: " + id));

        repository.delete(key);
        return ResponseEntity.noContent().build();
    }
}
