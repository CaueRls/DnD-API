package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.Spell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellRepository extends JpaRepository<Spell, Long> {


    Page<Spell> findByNameContainingIgnoreCase(String name, Pageable pageable);
}