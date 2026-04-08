package com.example.dungeons_dragons_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@Entity
@Table(name = "tb_classes")

public class CharacterClass extends RepresentationModel<CharacterClass> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "O nome da classe é obrigatório") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "O nome da classe é obrigatório") String name) {
        this.name = name;
    }

    public @NotNull(message = "O dado de vida (Hit Die) é obrigatório") HitDie getHitDie() {
        return hitDie;
    }

    public void setHitDie(@NotNull(message = "O dado de vida (Hit Die) é obrigatório") HitDie hitDie) {
        this.hitDie = hitDie;
    }

    public @NotBlank(message = "A descrição da classe não pode estar vazia") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "A descrição da classe não pode estar vazia") String description) {
        this.description = description;
    }

    public List<Subclass> getSubclasses() {
        return subclasses;
    }

    public void setSubclasses(List<Subclass> subclasses) {
        this.subclasses = subclasses;
    }

    public List<Spell> getSpells() {
        return spells;
    }

    public void setSpells(List<Spell> spells) {
        this.spells = spells;
    }

    @NotBlank(message = "O nome da classe é obrigatório")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O dado de vida (Hit Die) é obrigatório")
    private HitDie hitDie;

    @NotBlank(message = "A descrição da classe não pode estar vazia")
    @Column(columnDefinition = "TEXT")
    private String description;

    // 1. RELACIONAMENTO ONE-TO-MANY (Uma classe para várias subclasses)
    @OneToMany(mappedBy = "characterClass", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("characterClass") // Evita o loop infinito no JSON
    private List<Subclass> subclasses;

    // 2. RELACIONAMENTO MANY-TO-MANY (Muitas classes para muitas magias)
    @ManyToMany
    @JoinTable(
            name = "tb_class_spells",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "spell_id")
    )
    private List<Spell> spells;
}