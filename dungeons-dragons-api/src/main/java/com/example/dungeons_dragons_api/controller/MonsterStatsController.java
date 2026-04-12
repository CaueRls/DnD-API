package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.MonsterStats;
import com.example.dungeons_dragons_api.repository.MonsterStatsRepository;
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
@RequestMapping("/monster-stats")
@Tag(name = "Atributos de Monstros", description = "Operações relacionadas aos atributos (stats) dos monstros do D&D 5e")
public class MonsterStatsController {

    @Autowired
    private MonsterStatsRepository repository;

    private EntityModel<MonsterStats> toModel(MonsterStats s) {
        return EntityModel.of(s,
                linkTo(methodOn(MonsterStatsController.class).getStatsById(s.getId())).withSelfRel(),
                linkTo(methodOn(MonsterStatsController.class).updateStats(s.getId(), null)).withRel("update"),
                linkTo(methodOn(MonsterStatsController.class).deleteStats(s.getId())).withRel("delete"),
                linkTo(methodOn(MonsterStatsController.class).getAllStats(Pageable.unpaged(), null)).withRel("all-stats")
        );
    }

    @Operation(summary = "Lista todos os atributos de monstros", description = "Retorna uma lista paginada com todos os conjuntos de atributos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<MonsterStats>>> getAllStats(
            Pageable pageable, PagedResourcesAssembler<MonsterStats> assembler) {
        return ResponseEntity.ok(assembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(summary = "Busca atributos pelo ID", description = "Retorna os atributos de um monstro específico pelo ID.")
    @ApiResponse(responseCode = "200", description = "Atributos encontrados")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MonsterStats>> getStatsById(@PathVariable Long id) {
        MonsterStats s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id));
        return ResponseEntity.ok(toModel(s));
    }

    @Operation(summary = "Cria um novo conjunto de atributos",
            description = "Cadastra atributos avulsos. Todos os valores devem estar entre 1 e 30.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonsterStats.class),
                    examples = {
                            @ExampleObject(name = "Stats de Dragão", summary = "Criatura muito poderosa",
                                    value = """
                    {
                      "strength": 27, "dexterity": 14, "constitution": 25,
                      "intelligence": 16, "wisdom": 13, "charisma": 21
                    }
                    """),
                            @ExampleObject(name = "Stats de Goblin", summary = "Criatura fraca",
                                    value = """
                    {
                      "strength": 8, "dexterity": 14, "constitution": 10,
                      "intelligence": 10, "wisdom": 8, "charisma": 8
                    }
                    """),
                            @ExampleObject(name = "Stats Balanceados", summary = "Criatura equilibrada",
                                    value = """
                    {
                      "strength": 14, "dexterity": 14, "constitution": 14,
                      "intelligence": 14, "wisdom": 14, "charisma": 14
                    }
                    """)
                    }))
    @ApiResponse(responseCode = "201", description = "Atributos criados com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<MonsterStats>> createStats(@RequestBody @Valid MonsterStats stats) {
        return new ResponseEntity<>(toModel(repository.save(stats)), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza atributos existentes", description = "Atualiza todos os atributos de um monstro pelo ID.")
    @ApiResponse(responseCode = "200", description = "Atributos atualizados com sucesso")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MonsterStats>> updateStats(
            @PathVariable Long id, @RequestBody @Valid MonsterStats details) {
        MonsterStats s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id));
        s.setStrength(details.getStrength());
        s.setDexterity(details.getDexterity());
        s.setConstitution(details.getConstitution());
        s.setIntelligence(details.getIntelligence());
        s.setWisdom(details.getWisdom());
        s.setCharisma(details.getCharisma());
        return ResponseEntity.ok(toModel(repository.save(s)));
    }

    @Operation(summary = "Remove um conjunto de atributos", description = "Deleta permanentemente os atributos pelo ID.")
    @ApiResponse(responseCode = "204", description = "Atributos removidos com sucesso")
    @ApiResponse(responseCode = "404", description = "Atributos não encontrados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStats(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributos não encontrados com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filtra atributos por força mínima",
            description = "Consulta personalizada: retorna todos os conjuntos de atributos cuja força (strength) seja maior ou igual ao valor informado.")
    @ApiResponse(responseCode = "200", description = "Filtragem realizada com sucesso")
    @GetMapping("/filter")
    public ResponseEntity<PagedModel<EntityModel<MonsterStats>>> filterByMinStrength(
            @RequestParam Integer minStrength, Pageable pageable, PagedResourcesAssembler<MonsterStats> assembler) {
        return ResponseEntity.ok(assembler.toModel(
                repository.findByStrengthGreaterThanEqual(minStrength, pageable), this::toModel));
    }
}