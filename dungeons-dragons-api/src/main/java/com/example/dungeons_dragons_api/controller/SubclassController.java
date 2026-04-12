package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Subclass;
import com.example.dungeons_dragons_api.repository.SubclassRepository;
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
@RequestMapping("/subclasses")
@Tag(name = "Subclasses", description = "Operações relacionadas às subclasses de personagem do D&D 5e")
public class SubclassController {

    @Autowired
    private SubclassRepository repository;

    private EntityModel<Subclass> toModel(Subclass s) {
        return EntityModel.of(s,
                linkTo(methodOn(SubclassController.class).getSubclassById(s.getId())).withSelfRel(),
                linkTo(methodOn(SubclassController.class).updateSubclass(s.getId(), null)).withRel("update"),
                linkTo(methodOn(SubclassController.class).deleteSubclass(s.getId())).withRel("delete"),
                linkTo(methodOn(SubclassController.class).getAllSubclasses(Pageable.unpaged(), null)).withRel("all-subclasses")
        );
    }

    @Operation(summary = "Lista todas as subclasses", description = "Retorna uma lista paginada com todas as subclasses cadastradas.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Subclass>>> getAllSubclasses(
            Pageable pageable, PagedResourcesAssembler<Subclass> assembler) {
        return ResponseEntity.ok(assembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(summary = "Busca uma subclasse pelo ID", description = "Retorna os detalhes de uma subclasse específica, incluindo a classe pai.")
    @ApiResponse(responseCode = "200", description = "Subclasse encontrada")
    @ApiResponse(responseCode = "404", description = "Subclasse não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Subclass>> getSubclassById(@PathVariable Long id) {
        Subclass s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subclasse não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModel(s));
    }

    @Operation(summary = "Cria uma nova subclasse",
            description = "Cadastra uma nova subclasse vinculada a uma classe existente. O campo 'characterClass' deve conter o ID de uma classe já cadastrada.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Subclass.class),
                    examples = {
                            @ExampleObject(name = "Escola de Evocação", summary = "Subclasse do Mago",
                                    value = """
                    {
                      "name": "Escola de Evocação",
                      "description": "Especialistas em magias que canalizam energia para criar efeitos poderosos como fogo, relâmpagos e gelo.",
                      "characterClass": { "id": 1 }
                    }
                    """),
                            @ExampleObject(name = "Campeão", summary = "Subclasse do Guerreiro",
                                    value = """
                    {
                      "name": "Campeão",
                      "description": "Guerreiros focados no aperfeiçoamento físico, com maior chance de acertos críticos e recuperação aprimorada.",
                      "characterClass": { "id": 2 }
                    }
                    """),
                            @ExampleObject(name = "Domínio da Vida", summary = "Subclasse do Clérigo",
                                    value = """
                    {
                      "name": "Domínio da Vida",
                      "description": "Clérigos devotados à vida com poderes de cura aprimorados, capazes de restaurar vitalidade com grande eficiência.",
                      "characterClass": { "id": 3 }
                    }
                    """)
                    }))
    @ApiResponse(responseCode = "201", description = "Subclasse criada com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<Subclass>> createSubclass(@RequestBody @Valid Subclass subclass) {
        return new ResponseEntity<>(toModel(repository.save(subclass)), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza uma subclasse existente", description = "Atualiza todos os campos de uma subclasse pelo ID.")
    @ApiResponse(responseCode = "200", description = "Subclasse atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Subclasse não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Subclass>> updateSubclass(
            @PathVariable Long id, @RequestBody @Valid Subclass details) {
        Subclass s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subclasse não encontrada com o ID: " + id));
        s.setName(details.getName());
        s.setDescription(details.getDescription());
        s.setCharacterClass(details.getCharacterClass());
        return ResponseEntity.ok(toModel(repository.save(s)));
    }

    @Operation(summary = "Remove uma subclasse", description = "Deleta permanentemente uma subclasse pelo ID.")
    @ApiResponse(responseCode = "204", description = "Subclasse removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Subclasse não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubclass(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subclasse não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca subclasses pelo nome",
            description = "Consulta personalizada: retorna subclasses cujo nome contenha o termo informado (sem distinção de maiúsculas/minúsculas).")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Subclass>>> searchByName(
            @RequestParam String name, Pageable pageable, PagedResourcesAssembler<Subclass> assembler) {
        return ResponseEntity.ok(assembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }
}