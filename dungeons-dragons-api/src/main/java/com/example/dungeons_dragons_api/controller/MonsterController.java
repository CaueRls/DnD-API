package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Monster;
import com.example.dungeons_dragons_api.repository.MonsterRepository;
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
@RequestMapping("/monsters")
public class MonsterController {

    @Autowired
    private MonsterRepository repository;


    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Monster>>> getAllMonsters(
            Pageable pageable,
            PagedResourcesAssembler<Monster> assembler) {

        Page<Monster> monsters = repository.findAll(pageable);

        return ResponseEntity.ok(assembler.toModel(monsters, monster ->
                EntityModel.of(monster, linkTo(methodOn(MonsterController.class).getMonsterById(monster.getId())).withSelfRel())
        ));
    }


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Monster>> getMonsterById(@PathVariable Long id) {
        Monster monster = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Monstro não encontrado com o ID: " + id));

        EntityModel<Monster> resource = EntityModel.of(monster);
        resource.add(linkTo(methodOn(MonsterController.class).getMonsterById(id)).withSelfRel());
        resource.add(linkTo(methodOn(MonsterController.class).getAllMonsters(Pageable.unpaged(), null)).withRel("all-monsters"));

        return ResponseEntity.ok(resource);
    }


    @PostMapping
    public ResponseEntity<EntityModel<Monster>> createMonster(@RequestBody @Valid Monster monster) {
        Monster savedMonster = repository.save(monster);

        EntityModel<Monster> resource = EntityModel.of(savedMonster);
        resource.add(linkTo(methodOn(MonsterController.class).getMonsterById(savedMonster.getId())).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Monster>> updateMonster(@PathVariable Long id, @RequestBody @Valid Monster details) {
        Monster monster = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Monstro não encontrado com o ID: " + id));

        monster.setName(details.getName());
        monster.setType(details.getType());
        monster.setStats(details.getStats()); // Atualiza os atributos também

        Monster updated = repository.save(monster);
        EntityModel<Monster> resource = EntityModel.of(updated);
        resource.add(linkTo(methodOn(MonsterController.class).getMonsterById(updated.getId())).withSelfRel());

        return ResponseEntity.ok(resource);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonster(@PathVariable Long id) {
        Monster monster = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Monstro não encontrado com o ID: " + id));

        repository.delete(monster);
        return ResponseEntity.noContent().build();
    }
}