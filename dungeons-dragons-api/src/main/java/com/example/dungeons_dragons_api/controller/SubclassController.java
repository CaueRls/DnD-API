package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.model.Subclass;
import com.example.dungeons_dragons_api.repository.SubclassRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/subclasses")
public class SubclassController {

    @Autowired
    private SubclassRepository repository;

    @PostMapping
    public ResponseEntity<EntityModel<Subclass>> createSubclass(@RequestBody @Valid Subclass subclass) {
        Subclass saved = repository.save(subclass);
        EntityModel<Subclass> resource = EntityModel.of(saved);
        resource.add(linkTo(methodOn(SubclassController.class).getSubclassById(saved.getId())).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Subclass>> getSubclassById(@PathVariable Long id) {
        Subclass subclass = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subclasse não encontrada"));

        EntityModel<Subclass> resource = EntityModel.of(subclass);
        resource.add(linkTo(methodOn(SubclassController.class).getSubclassById(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }
}