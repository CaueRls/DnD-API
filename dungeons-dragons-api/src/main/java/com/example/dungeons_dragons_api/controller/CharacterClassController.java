package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.CharacterClass;
import com.example.dungeons_dragons_api.repository.CharacterClassRepository;
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
@RequestMapping("/classes")
@Tag(name = "Classes de Personagem", description = "Operações relacionadas às classes de personagem do D&D 5e")
public class CharacterClassController {

    @Autowired
    private CharacterClassRepository repository;

    @Operation(
            summary = "Lista todas as classes",
            description = "Retorna uma lista paginada com todas as classes de personagem cadastradas."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<CharacterClass>>> getAllClasses(
            Pageable pageable,
            PagedResourcesAssembler<CharacterClass> assembler) {

        Page<CharacterClass> classes = repository.findAll(pageable);
        return ResponseEntity.ok(assembler.toModel(classes, charClass ->
                EntityModel.of(charClass, linkTo(methodOn(CharacterClassController.class).getClassById(charClass.getId())).withSelfRel())
        ));
    }

    @Operation(
            summary = "Busca uma classe pelo ID",
            description = "Retorna os detalhes de uma classe de personagem específica."
    )
    @ApiResponse(responseCode = "200", description = "Classe encontrada")
    @ApiResponse(responseCode = "404", description = "Classe não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CharacterClass>> getClassById(@PathVariable Long id) {
        CharacterClass charClass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe não encontrada com o ID: " + id));

        EntityModel<CharacterClass> resource = EntityModel.of(charClass);
        resource.add(linkTo(methodOn(CharacterClassController.class).getClassById(id)).withSelfRel());
        resource.add(linkTo(methodOn(CharacterClassController.class).getAllClasses(Pageable.unpaged(), null)).withRel("all-classes"));
        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Cria uma nova classe de personagem",
            description = "Cadastra uma nova classe. O campo 'hitDie' deve ser um dos valores do enum HitDie (ex: D6, D8, D10, D12)."
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterClass.class),
                    examples = {
                            @ExampleObject(
                                    name = "Mago",
                                    summary = "Classe Mago",
                                    value = """
                    {
                      "name": "Mago",
                      "hitDie": "D6",
                      "description": "Um estudioso da magia arcana que conjura feitiços de poder devastador ou sutileza intrigante.",
                      "spells": []
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Guerreiro",
                                    summary = "Classe Guerreiro",
                                    value = """
                    {
                      "name": "Guerreiro",
                      "hitDie": "D10",
                      "description": "Um mestre do combate marcial, com grande proficiência em armas e armaduras de todos os tipos.",
                      "spells": []
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Clérigo",
                                    summary = "Classe Clérigo",
                                    value = """
                    {
                      "name": "Clérigo",
                      "hitDie": "D8",
                      "description": "Um intermediário entre o mundo mortal e o divino, capaz de curar aliados e destruir inimigos com o poder dos deuses.",
                      "spells": []
                    }
                    """
                            )
                    }
            )
    )
    @ApiResponse(responseCode = "201", description = "Classe criada com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<CharacterClass>> createClass(
            @org.springframework.web.bind.annotation.RequestBody @Valid CharacterClass charClass) {
        CharacterClass savedClass = repository.save(charClass);

        EntityModel<CharacterClass> resource = EntityModel.of(savedClass);
        resource.add(linkTo(methodOn(CharacterClassController.class).getClassById(savedClass.getId())).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualiza uma classe existente",
            description = "Atualiza todos os campos de uma classe de personagem pelo ID."
    )
    @ApiResponse(responseCode = "200", description = "Classe atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Classe não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CharacterClass>> updateClass(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid CharacterClass details) {
        CharacterClass charClass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe não encontrada com o ID: " + id));

        charClass.setName(details.getName());
        charClass.setHitDie(details.getHitDie());
        charClass.setDescription(details.getDescription());
        charClass.setSpells(details.getSpells());

        CharacterClass updated = repository.save(charClass);
        EntityModel<CharacterClass> resource = EntityModel.of(updated);
        resource.add(linkTo(methodOn(CharacterClassController.class).getClassById(updated.getId())).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Remove uma classe de personagem",
            description = "Deleta permanentemente uma classe pelo ID."
    )
    @ApiResponse(responseCode = "204", description = "Classe removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Classe não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        CharacterClass charClass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe não encontrada com o ID: " + id));

        repository.delete(charClass);
        return ResponseEntity.noContent().build();
    }
}