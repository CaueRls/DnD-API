package com.example.dungeons_dragons_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        // ← diz ao Swagger que TODOS os endpoints precisam do X-API-Key
        security = @SecurityRequirement(name = "X-API-Key")
)
// ← define como o header deve ser enviado
@SecurityScheme(
        name = "X-API-Key",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "Chave de API necessária para acessar os endpoints protegidos. " +
                "Gere uma chave em POST /api-keys antes de usar."
)
public class OpenApiConfig {
}