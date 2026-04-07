package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.CharacterClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterClassRepository extends JpaRepository<CharacterClass, Long> {
}