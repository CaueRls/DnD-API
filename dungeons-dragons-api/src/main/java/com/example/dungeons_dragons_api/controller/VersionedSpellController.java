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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/spells")
@Tag(
        name = "Magias por Header",
        description = "Endpoint demonstrativo de versionamento via header X-API-Version. Use X-API-Version: v1 ou X-API-Version: v2."
)
public class VersionedSpellController {

    private final SpellRepository repository;
    private final PagedResourcesAssembler<Spell> pagedAssembler;

    @Autowired
    public VersionedSpellController(SpellRepository repository,
                                    PagedResourcesAssembler<Spell> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    private EntityModel<Spell> toModelV1(Spell spell) {
        return EntityModel.of(spell,
                linkTo(methodOn(VersionedSpellController.class).getSpellByIdV1(spell.getId())).withSelfRel(),
                linkTo(methodOn(VersionedSpellController.class).updateSpellV1(spell.getId(), null)).withRel("update"),
                linkTo(methodOn(VersionedSpellController.class).deleteSpellV1(spell.getId())).withRel("delete"),
                linkTo(methodOn(VersionedSpellController.class).getAllSpellsV1(Pageable.unpaged())).withRel("all-spells"),
                linkTo(VersionedSpellController.class).slash("search").withRel("search-by-name"),
                linkTo(VersionedSpellController.class).withRel("latest-version")
        );
    }

    private EntityModel<Spell> toModelV2(Spell spell) {
        return EntityModel.of(spell,
                linkTo(methodOn(VersionedSpellController.class).getSpellByIdV2(spell.getId())).withSelfRel(),
                linkTo(methodOn(VersionedSpellController.class).updateSpellV2(spell.getId(), null)).withRel("update"),
                linkTo(methodOn(VersionedSpellController.class).deleteSpellV2(spell.getId())).withRel("delete"),
                linkTo(methodOn(VersionedSpellController.class).getAllSpellsV2(Pageable.unpaged())).withRel("all-spells"),
                linkTo(VersionedSpellController.class).slash("search").withRel("search-by-name"),
                linkTo(VersionedSpellController.class).withRel("previous-version")
        );
    }

    @Operation(
            summary = "Lista todas as magias [Header V1]",
            operationId = "getAllSpellsHeaderV1",
            description = "Retorna uma lista paginada de magias usando versionamento por header. Na V1, o campo castingTime não é alterado pelos endpoints de criação/atualização.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    description = "Versão da API. Para este endpoint use v1.",
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @GetMapping(headers = "X-API-Version=v1")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpellsV1(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(repository.findAll(pageable), this::toModelV1));
    }

    @Operation(
            summary = "Lista todas as magias [Header V2]",
            operationId = "getAllSpellsHeaderV2",
            description = "Retorna uma lista paginada de magias usando versionamento por header. A V2 inclui suporte completo ao campo castingTime.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    description = "Versão da API. Para este endpoint use v2.",
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @GetMapping(headers = "X-API-Version=v2")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> getAllSpellsV2(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(repository.findAll(pageable), this::toModelV2));
    }

    @Operation(
            summary = "Busca uma magia pelo ID [Header V1]",
            operationId = "getSpellByIdHeaderV1",
            description = "Busca uma magia específica pelo ID usando X-API-Version: v1.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping(value = "/{id}", headers = "X-API-Version=v1")
    public ResponseEntity<EntityModel<Spell>> getSpellByIdV1(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModelV1(spell));
    }

    @Operation(
            summary = "Busca uma magia pelo ID [Header V2]",
            operationId = "getSpellByIdHeaderV2",
            description = "Busca uma magia específica pelo ID usando X-API-Version: v2. Inclui suporte ao campo castingTime.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @ApiResponse(responseCode = "200", description = "Magia encontrada")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping(value = "/{id}", headers = "X-API-Version=v2")
    public ResponseEntity<EntityModel<Spell>> getSpellByIdV2(@PathVariable Long id) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        return ResponseEntity.ok(toModelV2(spell));
    }

    @Operation(
            summary = "Cria uma nova magia [Header V1]",
            operationId = "createSpellHeaderV1",
            description = "Cria uma magia usando X-API-Version: v1. A V1 ignora o campo castingTime para demonstrar diferença entre versões.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "Dados para criação de uma magia na versão v1.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Spell.class),
                    examples = @ExampleObject(name = "Bola de Fogo V1", value = """
                    {
                      "name": "Bola de Fogo",
                      "level": 3,
                      "description": "Uma rajada de chamas explode em um ponto escolhido.",
                      "school": "EVOCATION"
                    }
                    """)))
    @ApiResponse(responseCode = "201", description = "Magia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "409", description = "Já existe uma magia com o mesmo nome")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @PostMapping(headers = "X-API-Version=v1")
    public ResponseEntity<EntityModel<Spell>> createSpellV1(@RequestBody @Valid Spell spell) {
        if (repository.existsByNameIgnoreCase(spell.getName())) {
            throw new ResourceAlreadyExistsException("Já existe uma magia com o nome '" + spell.getName() + "'.");
        }
        spell.setCastingTime(null);
        return new ResponseEntity<>(toModelV1(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Cria uma nova magia [Header V2]",
            operationId = "createSpellHeaderV2",
            description = "Cria uma magia usando X-API-Version: v2. A V2 suporta o campo castingTime.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "Dados para criação de uma magia na versão v2.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Spell.class),
                    examples = @ExampleObject(name = "Bola de Fogo V2", value = """
                    {
                      "name": "Bola de Fogo",
                      "level": 3,
                      "description": "Uma rajada de chamas explode em um ponto escolhido.",
                      "school": "EVOCATION",
                      "castingTime": "1 action"
                    }
                    """)))
    @ApiResponse(responseCode = "201", description = "Magia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @ApiResponse(responseCode = "409", description = "Já existe uma magia com o mesmo nome")
    @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    @PostMapping(headers = "X-API-Version=v2")
    public ResponseEntity<EntityModel<Spell>> createSpellV2(@RequestBody @Valid Spell spell) {
        if (repository.existsByNameIgnoreCase(spell.getName())) {
            throw new ResourceAlreadyExistsException("Já existe uma magia com o nome '" + spell.getName() + "'.");
        }
        return new ResponseEntity<>(toModelV2(repository.save(spell)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualiza uma magia [Header V1]",
            operationId = "updateSpellHeaderV1",
            description = "Atualiza uma magia usando X-API-Version: v1. A V1 não atualiza castingTime.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @ApiResponse(responseCode = "200", description = "Magia atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @PutMapping(value = "/{id}", headers = "X-API-Version=v1")
    public ResponseEntity<EntityModel<Spell>> updateSpellV1(@PathVariable Long id, @RequestBody @Valid Spell details) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        spell.setName(details.getName());
        spell.setLevel(details.getLevel());
        spell.setDescription(details.getDescription());
        spell.setSchool(details.getSchool());
        return ResponseEntity.ok(toModelV1(repository.save(spell)));
    }

    @Operation(
            summary = "Atualiza uma magia [Header V2]",
            operationId = "updateSpellHeaderV2",
            description = "Atualiza uma magia usando X-API-Version: v2. A V2 permite atualizar castingTime.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @ApiResponse(responseCode = "200", description = "Magia atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @PutMapping(value = "/{id}", headers = "X-API-Version=v2")
    public ResponseEntity<EntityModel<Spell>> updateSpellV2(@PathVariable Long id, @RequestBody @Valid Spell details) {
        Spell spell = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id));
        spell.setName(details.getName());
        spell.setLevel(details.getLevel());
        spell.setDescription(details.getDescription());
        spell.setSchool(details.getSchool());
        spell.setCastingTime(details.getCastingTime());
        return ResponseEntity.ok(toModelV2(repository.save(spell)));
    }

    @Operation(
            summary = "Remove uma magia [Header V1]",
            operationId = "deleteSpellHeaderV1",
            description = "Remove uma magia usando X-API-Version: v1.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @DeleteMapping(value = "/{id}", headers = "X-API-Version=v1")
    public ResponseEntity<Void> deleteSpellV1(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove uma magia [Header V2]",
            operationId = "deleteSpellHeaderV2",
            description = "Remove uma magia usando X-API-Version: v2.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @ApiResponse(responseCode = "204", description = "Magia removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Magia não encontrada")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @DeleteMapping(value = "/{id}", headers = "X-API-Version=v2")
    public ResponseEntity<Void> deleteSpellV2(@PathVariable Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magia não encontrada com o ID: " + id)));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Busca magias pelo nome [Header V1]",
            operationId = "searchSpellByNameHeaderV1",
            description = "Consulta personalizada por entidade. Busca magias cujo nome contenha o termo informado usando X-API-Version: v1.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v1"}, defaultValue = "v1"))
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping(value = "/search", headers = "X-API-Version=v1")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByNameV1(
            @RequestParam String name,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModelV1));
    }

    @Operation(
            summary = "Busca magias pelo nome [Header V2]",
            operationId = "searchSpellByNameHeaderV2",
            description = "Consulta personalizada por entidade. Busca magias cujo nome contenha o termo informado usando X-API-Version: v2.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true,
                    schema = @Schema(type = "string", allowableValues = {"v2"}, defaultValue = "v2"))
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "401", description = "Header X-API-Key ausente")
    @ApiResponse(responseCode = "403", description = "API Key inválida ou inativa")
    @GetMapping(value = "/search", headers = "X-API-Version=v2")
    public ResponseEntity<PagedModel<EntityModel<Spell>>> searchByNameV2(
            @RequestParam String name,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                repository.findByNameContainingIgnoreCase(name, pageable), this::toModelV2));
    }
}
