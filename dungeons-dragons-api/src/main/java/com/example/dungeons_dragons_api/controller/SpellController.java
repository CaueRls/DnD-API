package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Spell;
import com.example.dungeons_dragons_api.repository.SpellRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/spells")
@Tag(name = "Magias", description = "Operações relacionadas às magias do D&D 5e")
public class SpellController {

    @Autowired
    private SpellRepository repository;

    private EntityModel<Spell> toModel(Spell spell) {
        return EntityModel.of(spell,
                linkTo(methodOn(SpellController.class).getSpellById(spell.getId())).withSelfRel(),
                linkTo(methodOn(SpellController.class).updateSpell(spell.getId(), null)).withRel("update"),
                linkTo(methodOn(SpellController.class).deleteSpell(spell.getId())).withRel("delete"),
                linkTo(methodOn(SpellController.class).getAllSpells(Pageable.unpaged(), null)).withRel("all-spells")
        );
    }

    @Operation(summary = "Lista todas as magias", description = "Retorna uma lista paginada com todas as magias cadastradas.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(
            Pageable pageable, PagedResourcesAssembler<Spell> assembler) {
        Page<Spell> spells = repository.findAll(pageable);
        return ResponseEntity.ok(assembler.toModel(spells, this::toModel));
    }

    @Operation(summary = "Busca uma magia pelo ID", description = "Retorna os detalhes de uma magia específica.")
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> getSpellById(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModel(spell));
    }

    @Operation(summary = "Cria uma nova magia",
            description = "Cadastra uma nova magia. O campo 'school' deve ser um dos valores do enum MagicSchool: ABJURATION, CONJURATION, DIVINATION, ENCHANTMENT, EVOCATION, ILLUSION, NECROMANCY, TRANSMUTATION.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Spell.class),
                    examples = {
                            @ExampleObject(name = "Bola de Fogo", summary = "Magia ofensiva nível 3",
                                    value = """
                    {
                      "name": "Bola de Fogo",
                      "level": 3,
                      "description": "Uma rajada de chamas explode em um ponto escolhido. Cada criatura em esfera de 20 pés deve realizar teste de Destreza.",
                      "school": "EVOCATION"
                    }
                    """),
                            @ExampleObject(name = "Curar Ferimentos", summary = "Magia de cura nível 1",
                                    value = """
                    {
                      "name": "Curar Ferimentos",
                      "level": 1,
                      "description": "Uma criatura que você toque recupera pontos de vida iguais a 1d8 + seu modificador de conjuração.",
                      "school": "EVOCATION"
                    }
                    """),
                            @ExampleObject(name = "Míssil Mágico", summary = "Magia ofensiva nível 1",
                                    value = """
                    {
                      "name": "Míssil Mágico",
                      "level": 1,
                      "description": "Três dardos de força mágica acertam automaticamente criaturas à sua escolha no alcance.",
                      "school": "EVOCATION"
                    }
                    """)
                    }))
    @ApiResponse(responseCode = "201", description = "Magia criada com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<Spell>> createSpell(@RequestBody @Valid Spell spell) {
        return new ResponseEntity<>(toModel(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza uma magia existente", description = "Atualiza todos os campos de uma magia pelo ID.")
    @ApiResponse(responseCode = "200", description = "Magia atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> updateSpell(
            @PathVariable Long id, @RequestBody @Valid Spell details) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id));
        spell.setName(details.getName());
        spell.setLevel(details.getLevel());
        spell.setDescription(details.getDescription());
        spell.setSchool(details.getSchool());
        return ResponseEntity.ok(toModel(repository.save(spell)));
    }

    @Operation(summary = "Remove uma magia", description = "Deleta permanentemente uma magia pelo ID.")
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpell(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca magias pelo nome",
            description = "Consulta personalizada: retorna magias cujo nome contenha o termo informado (sem distinção de maiúsculas/minúsculas).")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestParam String name, Pageable pageable, PagedResourcesAssembler<Spell> assembler) {
        return ResponseEntity.ok(assembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }
}