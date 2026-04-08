package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_spells")
public class Spell extends RepresentationModel<Spell> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da magia é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    @NotNull(message = "O nível da magia é obrigatório")
    @Min(0) @Max(9)
    private Integer level;

    @NotBlank(message = "A descrição não pode estar vazia")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A escola de magia é obrigatória")
    private MagicSchool school;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MagicSchool getSchool() { return school; }
    public void setSchool(MagicSchool school) { this.school = school; }
}