package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.MonsterStats;
import com.example.dungeons_dragons_api.repository.MonsterStatsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
@RequestMapping("/monster-stats")
@Tag(name = "Atributos de Monstros", description = "Operações relacionadas aos atributos (stats) dos monstros do D&D 5e")
public class MonsterStatsController {

    @Autowired
    private MonsterStatsRepository repository;

    @Operation(
        summary = "Lista todos os atributos de monstros",
        description = "Retorna uma lista paginada com todos os conjuntos de atributos cadastrados."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<MonsterStats>>> getAllStats(
            Pageable pageable,
            PagedResourcesAssembler<MonsterStats> assembler) {

        Page<MonsterStats> statsList = repository.findAll(pageable);
        return ResponseEntity.ok(assembler.toModel(statsList, stats ->
                EntityModel.of(stats, linkTo(methodOn(MonsterStatsController.class).getStatsById(stats.getId())).withSelfRel())
        ));
    }

    @Operation(
        summary = "Busca atributos pelo ID",
        description = "Retorna os atributos de um monstro específico pelo ID do stats."
    )
    @ApiResponse(responseCode = "200", description = "Atributos encontrados")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MonsterStats>> getStatsById(@PathVariable Long id) {
        MonsterStats stats = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id));

        EntityModel<MonsterStats> resource = EntityModel.of(stats);
        resource.add(linkTo(methodOn(MonsterStatsController.class).getStatsById(id)).withSelfRel());
        resource.add(linkTo(methodOn(MonsterStatsController.class).getAllStats(Pageable.unpaged(), null)).withRel("all-stats"));
        return ResponseEntity.ok(resource);
    }

    @Operation(
        summary = "Cria um novo conjunto de atributos",
        description = "Cadastra atributos avulsos. Todos os valores devem estar entre 1 e 30."
    )
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = MonsterStats.class),
            examples = {
                @ExampleObject(
                    name = "Stats de Dragão",
                    summary = "Atributos de criatura poderosa",
                    value = """
                    {
                      "strength": 27,
                      "dexterity": 14,
                      "constitution": 25,
                      "intelligence": 16,
                      "wisdom": 13,
                      "charisma": 21
                    }
                    """
                ),
                @ExampleObject(
                    name = "Stats de Goblin",
                    summary = "Atributos de criatura fraca",
                    value = """
                    {
                      "strength": 8,
                      "dexterity": 14,
                      "constitution": 10,
                      "intelligence": 10,
                      "wisdom": 8,
                      "charisma": 8
                    }
                    """
                ),
                @ExampleObject(
                    name = "Stats Balanceados",
                    summary = "Atributos equilibrados",
                    value = """
                    {
                      "strength": 14,
                      "dexterity": 14,
                      "constitution": 14,
                      "intelligence": 14,
                      "wisdom": 14,
                      "charisma": 14
                    }
                    """
                )
            }
        )
    )
    @ApiResponse(responseCode = "201", description = "Atributos criados com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<MonsterStats>> createStats(
            @org.springframework.web.bind.annotation.RequestBody @Valid MonsterStats stats) {
        MonsterStats saved = repository.save(stats);

        EntityModel<MonsterStats> resource = EntityModel.of(saved);
        resource.add(linkTo(methodOn(MonsterStatsController.class).getStatsById(saved.getId())).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualiza atributos existentes",
        description = "Atualiza todos os atributos de um monstro pelo ID."
    )
    @ApiResponse(responseCode = "200", description = "Atributos atualizados com sucesso")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MonsterStats>> updateStats(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid MonsterStats details) {
        MonsterStats stats = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id));

        stats.setStrength(details.getStrength());
        stats.setDexterity(details.getDexterity());
        stats.setConstitution(details.getConstitution());
        stats.setIntelligence(details.getIntelligence());
        stats.setWisdom(details.getWisdom());
        stats.setCharisma(details.getCharisma());

        MonsterStats updated = repository.save(stats);
        EntityModel<MonsterStats> resource = EntityModel.of(updated);
        resource.add(linkTo(methodOn(MonsterStatsController.class).getStatsById(updated.getId())).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @Operation(
        summary = "Remove um conjunto de atributos",
        description = "Deleta permanentemente os atributos pelo ID."
    )
    @ApiResponse(responseCode = "204", description = "Atributos removidos com sucesso")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStats(@PathVariable Long id) {
        MonsterStats stats = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id));

        repository.delete(stats);
        return ResponseEntity.noContent().build();
    }
}