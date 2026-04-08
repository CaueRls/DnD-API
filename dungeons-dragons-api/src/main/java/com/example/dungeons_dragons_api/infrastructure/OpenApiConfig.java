package com.example.dungeons_dragons_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "DnD API",
                version = "0.1.0",
                description = "Api criada para criar e verificar informações sobre o jogo Dungeons and Dragons.",
                contact = @Contact(
                        name = "Cauê Rodrigues",
                        email = "rlscaue2@gmail.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org" // URL to the MIT license
                )
        )
)
public class OpenApiConfig {
}
