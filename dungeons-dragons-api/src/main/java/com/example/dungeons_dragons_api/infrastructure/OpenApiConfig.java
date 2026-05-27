package com.example.dungeons_dragons_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "DnD API",
                version = "0.1.0",
                description = "API criada para cadastrar e consultar informações do jogo Dungeons & Dragons. " +
                        "Os endpoints protegidos usam autenticação por X-API-Key, idempotência por X-Idempotency-Key em POST e versionamento por X-API-Version.",
                contact = @Contact(
                        name = "Cauê Rodrigues",
                        email = "rlscaue2@gmail.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        )
)
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "Cole aqui apenas o valor da chave gerada em POST /api-keys, por exemplo: 123e4567-e89b-12d3-a456-426614174000."
)
public class OpenApiConfig {

    @Bean
    public OperationCustomizer headersAndSecurityCustomizer() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
            Class<?> controllerClass = handlerMethod.getBeanType();
            boolean isApiKeyController = controllerClass.getSimpleName().equals("ApiKeyController");
            boolean isPost = handlerMethod.hasMethodAnnotation(PostMapping.class);

            if (!isApiKeyController) {
                operation.addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));

                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-API-Version")
                        .required(false)
                        .description("Versão da API. Use v2 para a versão atual. Exemplo: v2")
                        .schema(new StringSchema()._default("v2")));
            }

            if (isPost && !isApiKeyController) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Idempotency-Key")
                        .required(true)
                        .description("Chave única para evitar criação duplicada em POST. Gere um UUID e reutilize a mesma chave apenas para repetir a mesma operação. Exemplo: 550e8400-e29b-41d4-a716-446655440000")
                        .schema(new StringSchema().example("550e8400-e29b-41d4-a716-446655440000")));
            }

            return operation;
        };
    }
}
