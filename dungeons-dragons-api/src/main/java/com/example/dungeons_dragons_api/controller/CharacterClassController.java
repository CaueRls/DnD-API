package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.CharacterClass;
import com.example.dungeons_dragons_api.repository.CharacterClassRepository;
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
public class CharacterClassController {

    @Autowired
    private CharacterClassRepository repository;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<CharacterClass>>> getAllClasses(
            Pageable pageable,
            PagedResourcesAssembler<CharacterClass> assembler) {

        Page<CharacterClass> classes = repository.findAll(pageable);

        return ResponseEntity.ok(assembler.toModel(classes, charClass ->
                EntityModel.of(charClass, linkTo(methodOn(CharacterClassController.class).getClassById(charClass.getId())).withSelfRel())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CharacterClass>> getClassById(@PathVariable Long id) {
        CharacterClass charClass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe não encontrada com o ID: " + id));

        EntityModel<CharacterClass> resource = EntityModel.of(charClass);

        resource.add(linkTo(methodOn(CharacterClassController.class).getClassById(id)).withSelfRel());

        // Ajustado para remover o aviso amarelo do null
        resource.add(linkTo(methodOn(CharacterClassController.class).getAllClasses(Pageable.unpaged(), null)).withRel("all-classes"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    public ResponseEntity<EntityModel<CharacterClass>> createClass(@RequestBody @Valid CharacterClass charClass) {
        CharacterClass savedClass = repository.save(charClass);

        EntityModel<CharacterClass> resource = EntityModel.of(savedClass);
        resource.add(linkTo(methodOn(CharacterClassController.class).getClassById(savedClass.getId())).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CharacterClass>> updateClass(@PathVariable Long id, @RequestBody @Valid CharacterClass details) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        CharacterClass charClass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe não encontrada com o ID: " + id));

        repository.delete(charClass);
        return ResponseEntity.noContent().build();
    }
} // <--- Verifique se essa última chave está presente no seu arquivo!