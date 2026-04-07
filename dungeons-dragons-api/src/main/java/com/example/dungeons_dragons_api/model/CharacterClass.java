package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@Entity
@Table(name = "tb_classes")
@Data
public class CharacterClass extends RepresentationModel<CharacterClass> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da classe é obrigatório")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O dado de vida (Hit Die) é obrigatório")
    private HitDie hitDie;

    @NotBlank(message = "A descrição da classe não pode estar vazia")
    @Column(columnDefinition = "TEXT")
    private String description;

    // RELACIONAMENTO MANY-TO-MANY
    @ManyToMany
    @JoinTable(
            name = "tb_class_spells", // Nome da tabela que o Hibernate vai criar
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "spell_id")
    )
    private List<Spell> spells;
}