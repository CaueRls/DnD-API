package com.example.dungeons_dragons_api.infrastructure;

import com.example.dungeons_dragons_api.controller.ApiKeyController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;

@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SECURITY_SCHEME = "ApiKeyAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DnD API")
                        .version("1.0.0")
                        .description("API REST para criação e consulta de informações sobre Dungeons & Dragons. Inclui HATEOAS, API Key, idempotência, rate limiting, CORS e versionamento.")
                        .contact(new Contact()
                                .name("Cauê Rodrigues")
                                .email("rlscaue2@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Chave de API. Gere uma chave em POST /api-keys e cole somente o valor de keyValue.")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SECURITY_SCHEME));
    }

    @Bean
    public OperationCustomizer commonResponsesAndHeadersCustomizer() {
        return (operation, handlerMethod) -> {
            Class<?> controllerClass = handlerMethod.getBeanType();
            boolean isApiKeyController = controllerClass.equals(ApiKeyController.class);
            boolean isPost = handlerMethod.hasMethodAnnotation(PostMapping.class);

            if (!isApiKeyController) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-API-Key")
                        .required(true)
                        .description("Chave de API ativa gerada em POST /api-keys. Use somente o valor de keyValue.")
                        .schema(new StringSchema().example("550e8400-e29b-41d4-a716-446655440000")));
            }

            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("X-API-Version")
                    .required(false)
                    .description("Versão da API. Use v2 por padrão. Rotas /v1/spells e /v2/spells também continuam disponíveis.")
                    .schema(new StringSchema().example("v2")));

            if (isPost && !isApiKeyController) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Idempotency-Key")
                        .required(true)
                        .description("Chave única para garantir que o mesmo POST não seja processado duas vezes. Reutilize a mesma chave apenas para repetir exatamente a mesma operação.")
                        .schema(new StringSchema().example("9f6a7e8d-8b90-4c0b-9e0b-917a1eddd111")));
            }

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            if (isPost) {
                responses.addApiResponse("201", new ApiResponse().description("Recurso criado com sucesso"));
            }

            responses.addApiResponse("400", new ApiResponse().description("Requisição inválida. Verifique o JSON, os parâmetros, enums e validações dos campos."));

            if (!isApiKeyController) {
                responses.addApiResponse("401", new ApiResponse().description("Header X-API-Key ausente"));
                responses.addApiResponse("403", new ApiResponse().description("API Key inválida ou inativa"));
                responses.addApiResponse("409", new ApiResponse().description("Conflito. Normalmente ocorre ao tentar cadastrar recurso duplicado."));
            }

            responses.addApiResponse("429", new ApiResponse().description("Limite de requisições excedido. Consulte o header Retry-After."));
            responses.addApiResponse("500", new ApiResponse().description("Erro interno inesperado do servidor"));

            return operation;
        };
    }
}
