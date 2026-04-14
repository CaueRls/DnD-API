package com.example.dungeons_dragons_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode =
		EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class DungeonsDragonsApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(DungeonsDragonsApiApplication.class, args);
	}
}