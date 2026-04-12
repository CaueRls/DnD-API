package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.Subclass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubclassRepository extends JpaRepository<Subclass, Long> {


    Page<Subclass> findByNameContainingIgnoreCase(String name, Pageable pageable);
}