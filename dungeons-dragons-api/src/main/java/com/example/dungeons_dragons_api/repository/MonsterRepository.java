package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.Monster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterRepository extends JpaRepository<Monster, Long> {


    Page<Monster> findByTypeContainingIgnoreCase(String type, Pageable pageable);
}