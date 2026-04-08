package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonsterRepository extends JpaRepository<Monster, Long> {
}