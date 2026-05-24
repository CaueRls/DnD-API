package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.exception.ResourceNotFoundException;
import com.example.dungeons_dragons_api.model.Spell;
import com.example.dungeons_dragons_api.repository.SpellRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(value = "/spells", headers = "X-API-Version=v2")
@Tag(name = "Magias V2", description = "Versão 2 — use o header 'X-API-Version: v2'. Inclui o campo castingTime (tempo de conjuração).")
public class SpellControllerV2 {

    private final SpellRepository repository;
    private final PagedResourcesAssembler<Spell> pagedAssembler;

    @Autowired
    public SpellControllerV2(SpellRepository repository,
                             PagedResourcesAssembler<Spell> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    private EntityModel<Spell> toModel(Spell spell) {
        return EntityModel.of(spell,
                linkTo(methodOn(SpellControllerV2.class).getSpellById(spell.getId())).withSelfRel(),
                linkTo(methodOn(SpellControllerV2.class).updateSpell(spell.getId(), null)).withRel("update"),
                linkTo(methodOn(SpellControllerV2.class).deleteSpell(spell.getId())).withRel("delete"),
                linkTo(methodOn(SpellControllerV2.class).getAllSpells(Pageable.unpaged())).withRel("all-spells")
        );
    }

    @Operation(
            summary = "Lista todas as magias [V2]",
            operationId = "getAllSpellsV2",
            description = "Requer header X-API-Version: v2. Retorna lista paginada incluindo o campo castingTime."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findAll(pageable), this::toModel));
    }

    @Operation(
            summary = "Busca uma magia pelo ID [V2]",
            operationId = "getSpellByIdV2",
            description = "Requer header X-API-Version: v2. Retorna os detalhes incluindo o campo castingTime."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> getSpellById(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModel(spell));
    }

    @Operation(
            summary = "Cria uma nova magia [V2]",
            operationId = "createSpellV2",
            description = "Requer header X-API-Version: v2. Suporta o campo castingTime. " +
                    "Valores comuns: '1 action', '1 bonus action', '1 reaction', '1 minute', '1 hour'."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Spell.class),
                    examples = {
                            @ExampleObject(name = "Bola de Fogo", summary = "Magia ofensiva nível 3",
                                    value = """
                                    {
                                      "name": "Bola de Fogo",
                                      "level": 3,
                                      "description": "Uma rajada de chamas explode em um ponto escolhido.",
                                      "school": "EVOCATION",
                                      "castingTime": "1 action"
                                    }
                                    """),
                            @ExampleObject(name = "Curar Ferimentos", summary = "Magia de cura nível 1",
                                    value = """
                                    {
                                      "name": "Curar Ferimentos",
                                      "level": 1,
                                      "description": "Uma criatura que você toque recupera pontos de vida.",
                                      "school": "EVOCATION",
                                      "castingTime": "1 action"
                                    }
                                    """),
                            @ExampleObject(name = "Palavra de Cura", summary = "Cura rápida nível 1",
                                    value = """
                                    {
                                      "name": "Palavra de Cura",
                                      "level": 1,
                                      "description": "Uma criatura de sua escolha recupera pontos de vida.",
                                      "school": "EVOCATION",
                                      "castingTime": "1 bonus action"
                                    }
                                    """)
                    }))
    @ApiResponse(responseCode = "201", description = "Magia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityModel<Spell>> createSpell(
            @org.springframework.web.bind.annotation.RequestBody
            @Valid Spell spell) {
        return new ResponseEntity<>(toModel(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualiza uma magia existente [V2]",
            operationId = "updateSpellV2",
            description = "Requer header X-API-Version: v2. Permite atualizar o campo castingTime além dos campos básicos."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @ApiResponse(responseCode = "200", description = "Magia atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> updateSpell(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody
            @Valid Spell details) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        spell.setName(details.getName());
        spell.setLevel(details.getLevel());
        spell.setDescription(details.getDescription());
        spell.setSchool(details.getSchool());
        spell.setCastingTime(details.getCastingTime()); // ← V2 atualiza castingTime
        return ResponseEntity.ok(toModel(repository.save(spell)));
    }

    @Operation(
            summary = "Remove uma magia [V2]",
            operationId = "deleteSpellV2",
            description = "Requer header X-API-Version: v2. Deleta permanentemente uma magia pelo ID."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpell(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Busca magias pelo nome [V2]",
            operationId = "searchByNameV2",
            description = "Requer header X-API-Version: v2. Retorna magias cujo nome contenha o termo informado. Inclui castingTime."
    )
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
            description = "Deve ser 'v2' para acessar este endpoint",
            schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestParam String name, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }
}