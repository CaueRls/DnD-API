package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Spell;
import com.example.dungeons_dragons_api.repository.SpellRepository;
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

// Este import static é o segredo para os links do HATEOAS funcionarem
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/spells")
public class SpellController {

    @Autowired
    private SpellRepository repository;

    // 1. LISTAR TODAS (Paginado + HATEOAS)
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(
            Pageable pageable,
            PagedResourcesAssembler<Spell> assembler) {

        Page<Spell> spells = repository.findAll(pageable);

        return ResponseEntity.ok(assembler.toModel(spells, spell ->
                EntityModel.of(spell, linkTo(methodOn(SpellController.class).getSpellById(spell.getId())).withSelfRel())
        ));
    }

    // 2. BUSCAR POR ID (HATEOAS)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> getSpellById(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id));

        EntityModel<Spell> resource = EntityModel.of(spell);
        resource.add(linkTo(methodOn(SpellController.class).getSpellById(id)).withSelfRel());
        resource.add(linkTo(methodOn(SpellController.class).getAllSpells(Pageable.unpaged(), null)).withRel("all-spells"));

        return ResponseEntity.ok(resource);
    }

    // 3. CRIAR NOVA (Validado + HATEOAS)
    @PostMapping
    public ResponseEntity<EntityModel<Spell>> createSpell(@RequestBody @Valid Spell spell) {
        Spell savedSpell = repository.save(spell);

        EntityModel<Spell> resource = EntityModel.of(savedSpell);
        resource.add(linkTo(methodOn(SpellController.class).getSpellById(savedSpell.getId())).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // 4. ATUALIZAR (Validado + HATEOAS)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> updateSpell(@PathVariable Long id, @RequestBody @Valid Spell spellDetails) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id));

        spell.setName(spellDetails.getName());
        spell.setLevel(spellDetails.getLevel());
        spell.setDescription(spellDetails.getDescription());
        spell.setSchool(spellDetails.getSchool());

        Spell updatedSpell = repository.save(spell);

        EntityModel<Spell> resource = EntityModel.of(updatedSpell);
        resource.add(linkTo(methodOn(SpellController.class).getSpellById(updatedSpell.getId())).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    // 5. DELETAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpell(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Magia não encontrada com o ID: " + id));

        repository.delete(spell);
        return ResponseEntity.noContent().build();
    }

    // EXTRA: BUSCA PERSONALIZADA (Requisito 3.4)
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestParam String name,
            Pageable pageable,
            PagedResourcesAssembler<Spell> assembler) {

        Page<Spell> spells = repository.findByNameContainingIgnoreCase(name, pageable);

        return ResponseEntity.ok(assembler.toModel(spells, spell ->
                EntityModel.of(spell, linkTo(methodOn(SpellController.class).getSpellById(spell.getId())).withSelfRel())
        ));
    }
}