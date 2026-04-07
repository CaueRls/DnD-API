package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_spells")
@Data // O Lombok cria os métodos getLevel, setLevel, etc., automaticamente
public class Spell extends RepresentationModel<Spell> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da magia é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    @NotNull(message = "O nível da magia deve ser informado")
    @Min(value = 0, message = "O nível mínimo é 0 (Truque)")
    @Max(value = 9, message = "O nível máximo é 9")
    private Integer level;

    @NotBlank(message = "A descrição não pode estar vazia")
    @Column(columnDefinition = "TEXT") // Essencial para textos longos de D&D
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A escola de magia é obrigatória")
    private MagicSchool school;
}