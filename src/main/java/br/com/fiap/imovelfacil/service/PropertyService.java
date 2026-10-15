package br.com.fiap.imovelfacil.service;

import java.math.BigDecimal;

import br.com.fiap.imovelfacil.dto.PropertyRequest;
import br.com.fiap.imovelfacil.model.Property;
import br.com.fiap.imovelfacil.model.PropertyType;
import br.com.fiap.imovelfacil.repository.PropertyRepository;
import br.com.fiap.imovelfacil.repository.PropertySpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private static final String CACHE_NAME = "properties";

    private final PropertyRepository repository;

    public Page<Property> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Query by Example: monta um "imóvel de exemplo" (probe) só com os filtros
     * recebidos. Os campos não informados ficam null e o matcher os ignora.
     */
    public Page<Property> findByExample(PropertyType type,
                                        String city,
                                        String neighborhood,
                                        Integer bedrooms,
                                        Pageable pageable) {

        Property probe = Property.builder()
                .type(type)
                .city(city)
                .neighborhood(neighborhood)
                .bedrooms(bedrooms)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        return repository.findAll(Example.of(probe, matcher), pageable);
    }

    /**
     * Specification: a busca que o Example não faz — faixas de valor e "contém".
     */
    public Page<Property> search(String title,
                                 String city,
                                 BigDecimal minPrice,
                                 BigDecimal maxPrice,
                                 Integer minBedrooms,
                                 Double minArea,
                                 Pageable pageable) {

        return repository.findAll(
                PropertySpecifications.search(title, city, minPrice, maxPrice, minBedrooms, minArea),
                pageable);
    }

    /**
     * O log só aparece quando o banco é realmente consultado:
     * na segunda leitura do mesmo id a resposta vem do cache e o método nem executa.
     */
    @Cacheable(value = CACHE_NAME, key = "#id")
    public Property findById(Long id) {
        log.info("🔎 Fetching property {} from the database", id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    }

    public Property create(PropertyRequest request) {
        return repository.save(request.toEntity());
    }

    // Atualiza a entrada do cache com o imóvel recém-salvo
    @CachePut(value = CACHE_NAME, key = "#id")
    public Property update(Long id, PropertyRequest request) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        Property property = request.toEntity();
        property.setId(id);
        return repository.save(property);
    }

    // Remove a entrada do cache para que o próximo GET responda 404
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        repository.deleteById(id);
    }
}