package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.CharacterClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterClassRepository extends JpaRepository<CharacterClass, Long> {


    Page<CharacterClass> findByNameContainingIgnoreCase(String name, Pageable pageable);
}