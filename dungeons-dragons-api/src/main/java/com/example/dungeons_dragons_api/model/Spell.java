package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "tb_spells")
@Data
// REMOVA o "extends RepresentationModel<Spell>"
public class Spell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da magia é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    // ... restante dos seus atributos
}