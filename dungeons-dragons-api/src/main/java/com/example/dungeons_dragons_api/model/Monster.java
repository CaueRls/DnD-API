package com.example.dungeons_dragons_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_monsters")
public class Monster extends RepresentationModel<Monster> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do monstro é obrigatório")
    private String name;

    @NotBlank(message = "O tipo do monstro é obrigatório")
    private String type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stats_id", referencedColumnName = "id")
    private MonsterStats stats;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MonsterStats getStats() {
        return stats;
    }

    public void setStats(MonsterStats stats) {
        this.stats = stats;
    }
}