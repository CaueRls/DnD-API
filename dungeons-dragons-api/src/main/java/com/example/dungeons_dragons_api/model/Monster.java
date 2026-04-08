package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_monsters")
@Data
public class Monster extends RepresentationModel<Monster> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do monstro é ")
    private String name;

    @NotBlank(message = "O tipo do monstro (ex: Dragão) ")
    private String type;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stats_id", referencedColumnName = "id")
    private MonsterStats stats;
}
