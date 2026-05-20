package com.example.dungeons_dragons_api.repository;

import com.example.dungeons_dragons_api.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {


    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
}