package com.example.dungeons_dragons_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_subclasses")
public class Subclass extends RepresentationModel<Subclass> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da subclasse é obrigatório")
    private String name;

    @NotBlank(message = "A descrição da subclasse é obrigatória")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "class_id")
    @NotNull(message = "A classe pai é obrigatória")
    @JsonIgnoreProperties("subclasses")
    private CharacterClass characterClass;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CharacterClass getCharacterClass() { return characterClass; }
    public void setCharacterClass(CharacterClass characterClass) { this.characterClass = characterClass; }
}