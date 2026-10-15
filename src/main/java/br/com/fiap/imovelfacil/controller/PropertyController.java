package br.com.fiap.imovelfacil.controller;

import java.math.BigDecimal;

import br.com.fiap.imovelfacil.dto.PropertyRequest;
import br.com.fiap.imovelfacil.hateoas.PropertyModelAssembler;
import br.com.fiap.imovelfacil.model.Property;
import br.com.fiap.imovelfacil.model.PropertyType;
import br.com.fiap.imovelfacil.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Imóveis", description = "Catálogo de imóveis para locação: listagem, buscas e CRUD")
public class PropertyController {

    private final PropertyService service;

    // Converte um imóvel em EntityModel (com os links self e properties)
    private final PropertyModelAssembler assembler;

    // Converte uma Page em PagedModel, já com os links first, prev, next e last
    private final PagedResourcesAssembler<Property> pagedAssembler;

    @GetMapping
    @Operation(summary = "Lista os imóveis de forma paginada")
    public PagedModel<EntityModel<Property>> findAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return pagedAssembler.toModel(service.findAll(pageable), assembler);
    }

    @GetMapping("/example")
    @Operation(summary = "Busca imóveis iguais a um imóvel de exemplo (Query by Example)")
    public PagedModel<EntityModel<Property>> findByExample(
            @Parameter(description = "Tipo do imóvel: APARTMENT, HOUSE, STUDIO ou COMMERCIAL")
            @RequestParam(required = false) PropertyType type,

            @Parameter(description = "Cidade exata, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String city,

            @Parameter(description = "Bairro exato, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String neighborhood,

            @Parameter(description = "Número exato de quartos")
            @RequestParam(required = false) Integer bedrooms,

            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return pagedAssembler.toModel(
                service.findByExample(type, city, neighborhood, bedrooms, pageable),
                assembler);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca imóveis por faixas de valor e por trecho do título (Specification)")
    public PagedModel<EntityModel<Property>> search(
            @Parameter(description = "Trecho do título, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String title,

            @Parameter(description = "Cidade exata, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String city,

            @Parameter(description = "Aluguel mensal mínimo, em reais")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Aluguel mensal máximo, em reais")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Número mínimo de quartos")
            @RequestParam(required = false) Integer minBedrooms,

            @Parameter(description = "Área mínima, em m²")
            @RequestParam(required = false) Double minArea,

            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return pagedAssembler.toModel(
                service.search(title, city, minPrice, maxPrice, minBedrooms, minArea, pageable),
                assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um imóvel pelo id")
    @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    public EntityModel<Property> findById(@PathVariable Long id) {
        return assembler.toModel(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo imóvel")
    @ApiResponse(responseCode = "400", description = "Dados do imóvel inválidos")
    public ResponseEntity<EntityModel<Property>> create(@RequestBody @Valid PropertyRequest request) {
        EntityModel<Property> model = assembler.toModel(service.create(request));

        // O Location reaproveita o link self que o assembler acabou de montar
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um imóvel existente")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Dados do imóvel inválidos"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    })
    public EntityModel<Property> update(@PathVariable Long id,
                                        @RequestBody @Valid PropertyRequest request) {
        return assembler.toModel(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um imóvel")
    @ApiResponse(responseCode = "404", description = "Imóvel não encontrado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}