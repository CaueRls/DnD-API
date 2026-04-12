package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.MonsterStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterStatsRepository extends JpaRepository<MonsterStats, Long> {


    Page<MonsterStats> findByStrengthGreaterThanEqual(Integer strength, Pageable pageable);
}