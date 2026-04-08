package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.Subclass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubclassRepository extends JpaRepository<Subclass, Long> {
}