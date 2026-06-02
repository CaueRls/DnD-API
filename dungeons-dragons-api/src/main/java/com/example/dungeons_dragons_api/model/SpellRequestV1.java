package com.example.dungeons_dragons_api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para criação de magia na V1 — campos básicos apenas")
public class SpellRequestV1 {

    @NotBlank(message = "O nome da magia é obrigatório")
    @Size(min = 3, max = 100)
    @Schema(description = "Nome da magia", example = "Bola de Fogo")
    private String name;

    @NotNull(message = "O nível da magia é obrigatório")
    @Min(0) @Max(9)
    @Schema(description = "Nível da magia (0 = truque, 1-9 = magia)", example = "3")
    private Integer level;

    @NotBlank(message = "A descrição não pode estar vazia")
    @Schema(description = "Descrição da magia", example = "Uma rajada de chamas explode em um ponto escolhido.")
    private String description;

    @NotNull(message = "A escola de magia é obrigatória")
    @Schema(description = "Escola de magia", example = "EVOCATION",
            allowableValues = {"ABJURATION","CONJURATION","DIVINATION","ENCHANTMENT","EVOCATION","ILLUSION","NECROMANCY","TRANSMUTATION"})
    private MagicSchool school;

    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MagicSchool getSchool() { return school; }
    public void setSchool(MagicSchool school) { this.school = school; }
}