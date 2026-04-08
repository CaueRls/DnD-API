package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_monster_stats")
@Data
public class MonsterStats extends RepresentationModel<MonsterStats> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1) @Max(30)
    private Integer strength;

    @Min(1) @Max(30)
    private Integer dexterity;

    @Min(1) @Max(30)
    private Integer constitution;

    @Min(1) @Max(30)
    private Integer intelligence;

    @Min(1) @Max(30)
    private Integer wisdom;

    @Min(1) @Max(30)
    private Integer charisma;
}