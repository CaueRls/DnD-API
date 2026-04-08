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

    @NotBlank(message = "O nome da classe é obrigatório")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O dado de vida é obrigatório")
    private HitDie hitDie;

    @NotBlank(message = "A descrição não pode estar vazia")
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "characterClass", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("characterClass")
    private List<Subclass> subclasses;

    @ManyToMany
    @JoinTable(
            name = "tb_class_spells",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "spell_id")
    )
    private List<Spell> spells;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HitDie getHitDie() { return hitDie; }
    public void setHitDie(HitDie hitDie) { this.hitDie = hitDie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Subclass> getSubclasses() { return subclasses; }
    public void setSubclasses(List<Subclass> subclasses) { this.subclasses = subclasses; }

    public List<Spell> getSpells() { return spells; }
    public void setSpells(List<Spell> spells) { this.spells = spells; }
}