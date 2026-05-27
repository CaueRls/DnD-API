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
                version = "1.0.0",
                description = "API para criar e consultar informações de Dungeons & Dragons. " +
                        "Recursos implementados: CRUD paginado, HATEOAS, Bean Validation, API Key, " +
                        "idempotência com X-Idempotency-Key, rate limiting, CORS e versionamento por X-API-Version.",
                contact = @Contact(
                        name = "Cauê Rodrigues",
                        email = "rlscaue2@gmail.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        security = {
                @SecurityRequirement(name = "X-API-Key"),
                @SecurityRequirement(name = "X-API-Version")
        }
)
@SecurityScheme(
        name = "X-API-Key",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "Chave de API necessária para acessar os endpoints protegidos. Gere uma chave em POST /api-keys?owner=seu-nome."
)
@SecurityScheme(
        name = "X-API-Version",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Version",
        description = "Versão da API. Use v1 ou v2 nos endpoints versionados por header, por exemplo: GET /spells com X-API-Version: v2."
)
public class OpenApiConfig {
}
