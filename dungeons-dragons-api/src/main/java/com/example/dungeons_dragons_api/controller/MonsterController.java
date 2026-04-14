package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Monster;
import com.example.dungeons_dragons_api.repository.MonsterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import com.example.dungeons_dragons_api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/monsters")
@Tag(name = "Monstros", description = "Operações relacionadas aos monstros do D&D 5e")
public class MonsterController {

    @Autowired
    private MonsterRepository repository;

    private EntityModel<Monster> toModel(Monster m) {
        return EntityModel.of(m,
                linkTo(methodOn(MonsterController.class).getMonsterById(m.getId())).withSelfRel(),
                linkTo(methodOn(MonsterController.class).updateMonster(m.getId(), null)).withRel("update"),
                linkTo(methodOn(MonsterController.class).deleteMonster(m.getId())).withRel("delete"),
                linkTo(methodOn(MonsterController.class).getAllMonsters(Pageable.unpaged(), null)).withRel("all-monsters")
        );
    }

    @Operation(summary = "Lista todos os monstros", description = "Retorna uma lista paginada com todos os monstros cadastrados, incluindo seus atributos.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Monster>>> getAllMonsters(
            Pageable pageable, PagedResourcesAssembler<Monster> assembler) {
        return ResponseEntity.ok(assembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(summary = "Busca um monstro pelo ID", description = "Retorna os detalhes de um monstro específico, incluindo seus atributos.")
    @ApiResponse(responseCode = "200", description = "Monstro encontrado")
    @ApiResponse(responseCode = "404", description = "Monstro não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Monster>> getMonsterById(@PathVariable Long id) {
        Monster m = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monstro não encontrado com o ID: " + id));
        return ResponseEntity.ok(toModel(m));
    }

    @Operation(summary = "Cria um novo monstro",
            description = "Cadastra um novo monstro junto com seus atributos (stats). Todos os atributos devem ter valores entre 1 e 30.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Monster.class),
                    examples = {
                            @ExampleObject(name = "Dragão Vermelho", summary = "Dragão Vermelho Ancião",
                                    value = """
                    {
                      "name": "Dragão Vermelho Ancião",
                      "type": "Dragão",
                      "stats": {
                        "strength": 30, "dexterity": 10, "constitution": 29,
                        "intelligence": 18, "wisdom": 15, "charisma": 23
                      }
                    }
                    """),
                            @ExampleObject(name = "Goblin", summary = "Goblin comum",
                                    value = """
                    {
                      "name": "Goblin",
                      "type": "Humanoide",
                      "stats": {
                        "strength": 8, "dexterity": 14, "constitution": 10,
                        "intelligence": 10, "wisdom": 8, "charisma": 8
                      }
                    }
                    """),
                            @ExampleObject(name = "Lich", summary = "Lich poderoso",
                                    value = """
                    {
                      "name": "Lich",
                      "type": "Morto-vivo",
                      "stats": {
                        "strength": 11, "dexterity": 16, "constitution": 16,
                        "intelligence": 20, "wisdom": 14, "charisma": 16
                      }
                    }
                    """)
                    }))
    @ApiResponse(responseCode = "201", description = "Monstro criado com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<Monster>> createMonster(@RequestBody @Valid Monster monster) {
        return new ResponseEntity<>(toModel(repository.save(monster)), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza um monstro existente", description = "Atualiza todos os campos de um monstro pelo ID, incluindo seus atributos.")
    @ApiResponse(responseCode = "200", description = "Monstro atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Monstro não encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Monster>> updateMonster(
            @PathVariable Long id, @RequestBody @Valid Monster details) {
        Monster m = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monstro não encontrado com o ID: " + id));
        m.setName(details.getName());
        m.setType(details.getType());
        m.setStats(details.getStats());
        return ResponseEntity.ok(toModel(repository.save(m)));
    }

    @Operation(summary = "Remove um monstro", description = "Deleta permanentemente um monstro pelo ID.")
    @ApiResponse(responseCode = "204", description = "Monstro removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Monstro não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonster(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monstro não encontrado com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca monstros pelo tipo",
            description = "Consulta personalizada: retorna monstros cujo tipo contenha o termo informado (ex: Dragão, Humanoide, Morto-vivo).")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Monster>>> searchByType(
            @RequestParam String type, Pageable pageable, PagedResourcesAssembler<Monster> assembler) {
        return ResponseEntity.ok(assembler.toModel(
                repository.findByTypeContainingIgnoreCase(type, pageable), this::toModel));
    }
}