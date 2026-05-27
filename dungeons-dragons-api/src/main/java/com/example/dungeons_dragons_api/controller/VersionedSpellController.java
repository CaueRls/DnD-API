package com.example.dungeons_dragons_api.controller;

import com.example.dungeons_dragons_api.exception.ResourceAlreadyExistsException;
import com.example.dungeons_dragons_api.exception.ResourceNotFoundException;
import com.example.dungeons_dragons_api.model.Spell;
import com.example.dungeons_dragons_api.repository.SpellRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/spells")
@Tag(
        name = "Magias por Header",
        description = "Endpoint demonstrativo de versionamento via header X-API-Version. Use v1 ou v2."
)
@SecurityRequirement(name = "X-API-Key")
public class VersionedSpellController {

    private final SpellRepository repository;
    private final PagedResourcesAssembler<Spell> pagedAssembler;

    @Autowired
    public VersionedSpellController(SpellRepository repository,
                                    PagedResourcesAssembler<Spell> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    private boolean isV1(String version) {
        return "v1".equalsIgnoreCase(version);
    }

    private EntityModel<Spell> toModel(Spell spell) {
        return EntityModel.of(spell,
                linkTo(VersionedSpellController.class).slash(spell.getId()).withSelfRel(),
                linkTo(VersionedSpellController.class).slash(spell.getId()).withRel("update"),
                linkTo(VersionedSpellController.class).slash(spell.getId()).withRel("delete"),
                linkTo(VersionedSpellController.class).withRel("all-spells"),
                linkTo(VersionedSpellController.class).slash("search").withRel("search-by-name"),
                linkTo(SpellController.class).withRel("v1-url-version"),
                linkTo(SpellControllerV2.class).withRel("v2-url-version")
        );
    }

    @Operation(
            summary = "Lista todas as magias usando versionamento por header",
            operationId = "getAllSpellsHeaderVersioned",
            description = "Retorna uma lista paginada de magias. Envie X-API-Version: v1 ou X-API-Version: v2 para demonstrar versionamento por cabeçalho.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            )
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpells(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(repository.findAll(pageable), this::toModel));
    }

    @Operation(
            summary = "Busca uma magia pelo ID usando versionamento por header",
            operationId = "getSpellByIdHeaderVersioned",
            description = "Busca uma magia específica pelo ID. Envie X-API-Version: v1 ou v2.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            )
    )
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> getSpellById(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModel(spell));
    }

    @Operation(
            summary = "Cria uma nova magia usando versionamento por header",
            operationId = "createSpellHeaderVersioned",
            description = "Cria uma magia. Na versão v1, o campo castingTime é ignorado; na versão v2, ele é salvo normalmente.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados para criação de uma magia.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Spell.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Bola de Fogo",
                                      "level": 3,
                                      "description": "Uma rajada de chamas explode em um ponto escolhido.",
                                      "school": "EVOCATION",
                                      "castingTime": "1 action"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Magia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "409", description = "Já existe uma magia com o mesmo nome")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @PostMapping
    public ResponseEntity<EntityModel<Spell>> createSpell(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @RequestBody @Valid Spell spell) {
        if (repository.existsByNameIgnoreCase(spell.getName())) {
            throw new ResourceAlreadyExistsException("Já existe uma magia com o nome '" + spell.getName() + "'.");
        }
        if (isV1(version)) {
            spell.setCastingTime(null);
        }
        return new ResponseEntity<>(toModel(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualiza uma magia usando versionamento por header",
            operationId = "updateSpellHeaderVersioned",
            description = "Atualiza uma magia. Na versão v1, o campo castingTime não é atualizado; na versão v2, ele é atualizado normalmente.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            )
    )
    @ApiResponse(responseCode = "200", description = "Magia atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Spell>> updateSpell(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @PathVariable Long id,
            @RequestBody @Valid Spell details) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));

        spell.setName(details.getName());
        spell.setLevel(details.getLevel());
        spell.setDescription(details.getDescription());
        spell.setSchool(details.getSchool());

        if (!isV1(version)) {
            spell.setCastingTime(details.getCastingTime());
        }

        return ResponseEntity.ok(toModel(repository.save(spell)));
    }

    @Operation(
            summary = "Remove uma magia usando versionamento por header",
            operationId = "deleteSpellHeaderVersioned",
            description = "Remove uma magia. Envie X-API-Version: v1 ou v2.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            )
    )
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpell(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Busca magias pelo nome usando versionamento por header",
            operationId = "searchSpellByNameHeaderVersioned",
            description = "Consulta personalizada por entidade. Busca magias cujo nome contenha o termo informado. Envie X-API-Version: v1 ou v2.",
            parameters = @Parameter(
                    name = "X-API-Version",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Versão da API. Use v1 ou v2.",
                    schema = @Schema(type = "string", allowableValues = {"v1", "v2"}, defaultValue = "v2")
            )
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByName(
            @RequestHeader(name = "X-API-Version", defaultValue = "v2") String version,
            @RequestParam String name,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModel));
    }
}
