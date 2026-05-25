package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.exception.ResourceAlreadyExistsException;
import com.example.dungeons_dragons_api.exception.ResourceNotFoundException;
import com.example.dungeons_dragons_api.model.Spell;
import com.example.dungeons_dragons_api.repository.SpellRepository;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/v1/spells")
@Tag(name = "Magias", description = "Versão 1 — operações básicas de magias do D&D 5e (sem castingTime)")
public class SpellController {

    private final SpellRepository repository;
    private final PagedResourcesAssembler<Spell> pagedAssembler;

    @Autowired
    public SpellController(SpellRepository repository,
                           PagedResourcesAssembler<Spell> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    // ← CORRIGIDO: links apontam para SpellController (V1), não V2
    private EntityModel<Spell> toModel(Spell spell) {
        return EntityModel.of(spell,
                linkTo(methodOn(SpellController.class).getSpellById(spell.getId())).withSelfRel(),
                linkTo(methodOn(SpellController.class).updateSpell(spell.getId(), null)).withRel("update"),
                linkTo(methodOn(SpellController.class).deleteSpell(spell.getId())).withRel("delete"),
                linkTo(methodOn(SpellController.class).getAllSpells(Pageable.unpaged())).withRel("all-spells")
        );
    }

    @Operation(
            summary = "Lista todas as magias",
            operationId = "getAllSpells",
            description = "Versão 1. Retorna lista paginada com todas as magias. Não inclui castingTime."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(
            summary = "Busca uma magia pelo ID",
            operationId = "getSpellById",
            description = "Versão 1. Retorna os detalhes de uma magia específica."
    )
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> getSpellById(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModel(spell));
    }

    @Operation(
            summary = "Cria uma nova magia",
            operationId = "createSpell",
            description = "Versão 1. Cadastra uma nova magia sem o campo castingTime. " +
                    "O campo 'school' deve ser: ABJURATION, CONJURATION, DIVINATION, ENCHANTMENT, EVOCATION, ILLUSION, NECROMANCY, TRANSMUTATION."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Spell.class),
                    examples = {
                            @ExampleObject(name = "Bola de Fogo", summary = "Magia ofensiva nível 3",
                                    value = """
                                    {
                                      "name": "Bola de Fogo",
                                      "level": 3,
                                      "description": "Uma rajada de chamas explode em um ponto escolhido.",
                                      "school": "EVOCATION"
                                    }
                                    """),
                            @ExampleObject(name = "Curar Ferimentos", summary = "Magia de cura nível 1",
                                    value = """
                                    {
                                      "name": "Curar Ferimentos",
                                      "level": 1,
                                      "description": "Uma criatura que você toque recupera pontos de vida.",
                                      "school": "EVOCATION"
                                    }
                                    """),
                            @ExampleObject(name = "Míssil Mágico", summary = "Magia ofensiva nível 1",
                                    value = """
                                    {
                                      "name": "Míssil Mágico",
                                      "level": 1,
                                      "description": "Três dardos de força mágica acertam automaticamente criaturas no alcance.",
                                      "school": "EVOCATION"
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

        // ← verificação de duplicidade
        if (repository.existsByNameIgnoreCase(spell.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Já existe uma magia com o nome '" + spell.getName() + "'.");
        }

        return new ResponseEntity<>(toModel(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualiza uma magia existente",
            operationId = "updateSpell",
            description = "Versão 1. Atualiza os campos básicos de uma magia. Não atualiza castingTime."
    )
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
        // ← V1 não atualiza castingTime intencionalmente
        return ResponseEntity.ok(toModel(repository.save(spell)));
    }

    @Operation(
            summary = "Remove uma magia",
            operationId = "deleteSpell",
            description = "Versão 1. Deleta permanentemente uma magia pelo ID."
    )
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpell(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Busca magias pelo nome",
            operationId = "searchByName",
            description = "Versão 1. Consulta personalizada por nome (sem distinção de maiúsculas/minúsculas)."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestParam String name, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }

}