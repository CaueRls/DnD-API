package com.example.dungeons_dragons_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_subclasses")
@Data
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
    @JoinColumn(name = "class_id") // Cria uma chave estrangeira no banco
    @NotNull(message = "A classe pai é obrigatória")
    @JsonIgnoreProperties("subclasses") // Evita que o JSON entre em loop infinito
    private CharacterClass characterClass;
}