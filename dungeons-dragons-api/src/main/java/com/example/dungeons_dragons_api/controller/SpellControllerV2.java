package com.example.dungeons_dragons_api.controller;

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
@RequestMapping(value = "/spells", headers = "X-API-Version=v2")
@Tag(name = "Magias V2", description = "Versão 2 das operações de magias — inclui o campo castingTime (tempo de conjuração)")
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
            description = "Retorna uma lista paginada com todas as magias cadastradas. Esta versão inclui o campo castingTime."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(
            summary = "Busca uma magia pelo ID [V2]",
            description = "Retorna os detalhes de uma magia específica, incluindo o campo castingTime."
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
            summary = "Cria uma nova magia [V2]",
            description = "Versão 2: agora suporta o campo castingTime (tempo de conjuração). " +
                    "Valores comuns: '1 action', '1 bonus action', '1 reaction', '1 minute', '10 minutes', '1 hour'. " +
                    "O campo 'school' deve ser um dos valores: ABJURATION, CONJURATION, DIVINATION, ENCHANTMENT, EVOCATION, ILLUSION, NECROMANCY, TRANSMUTATION."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Spell.class),
                    examples = {
                            @ExampleObject(name = "Bola de Fogo", summary = "Magia ofensiva nível 3",
                                    value = """
                    {
                      "name": "Bola de Fogo",
                      "level": 3,
                      "description": "Uma rajada de chamas explode em um ponto escolhido. Cada criatura em esfera de 20 pés deve realizar teste de Destreza.",
                      "school": "EVOCATION",
                      "castingTime": "1 action"
                    }
                    """),
                            @ExampleObject(name = "Curar Ferimentos", summary = "Magia de cura nível 1",
                                    value = """
                    {
                      "name": "Curar Ferimentos",
                      "level": 1,
                      "description": "Uma criatura que você toque recupera pontos de vida iguais a 1d8 + seu modificador de conjuração.",
                      "school": "EVOCATION",
                      "castingTime": "1 action"
                    }
                    """),
                            @ExampleObject(name = "Míssil Mágico", summary = "Magia ofensiva nível 1",
                                    value = """
                    {
                      "name": "Míssil Mágico",
                      "level": 1,
                      "description": "Três dardos de força mágica acertam automaticamente criaturas à sua escolha no alcance.",
                      "school": "EVOCATION",
                      "castingTime": "1 action"
                    }
                    """),
                            @ExampleObject(name = "Palavra de Cura em Massa", summary = "Magia de cura em área nível 3",
                                    value = """
                    {
                      "name": "Palavra de Cura em Massa",
                      "level": 3,
                      "description": "Até seis criaturas de sua escolha que você possa ver recuperam pontos de vida.",
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
            description = "Versão 2: permite atualizar o campo castingTime além dos demais campos."
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
        spell.setCastingTime(details.getCastingTime()); // ← campo novo da v2
        return ResponseEntity.ok(toModel(repository.save(spell)));
    }

    @Operation(
            summary = "Remove uma magia [V2]",
            description = "Deleta permanentemente uma magia pelo ID."
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
            summary = "Busca magias pelo nome [V2]",
            description = "Consulta personalizada: retorna magias cujo nome contenha o termo informado. Resposta inclui castingTime."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestParam String name, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }
}