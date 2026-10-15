package br.com.fiap.imovelfacil.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.imovelfacil.model.Property;
import org.springframework.data.jpa.domain.Specification;

/**
 * Uma Specification por filtro, como pede o enunciado.
 * Cada método devolve apenas o seu próprio critério; quem junta tudo é o search().
 */
public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    // title: contém, sem diferenciar maiúsculas de minúsculas
    public static Specification<Property> titleContains(String title) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    // city: igual, sem diferenciar maiúsculas de minúsculas
    public static Specification<Property> cityEquals(String city) {
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Property> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Property> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Property> bedroomsGreaterThanOrEqual(Integer minBedrooms) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("bedrooms"), minBedrooms);
    }

    public static Specification<Property> areaGreaterThanOrEqual(Double minArea) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("area"), minArea);
    }

    /**
     * Combina com "and" apenas os filtros que vieram preenchidos.
     * Começa de uma condição sempre verdadeira (conjunction) para que
     * uma busca sem nenhum filtro devolva todos os imóveis.
     */
    public static Specification<Property> search(String title,
                                                 String city,
                                                 BigDecimal minPrice,
                                                 BigDecimal maxPrice,
                                                 Integer minBedrooms,
                                                 Double minArea) {

        List<Specification<Property>> filtrosInformados = new ArrayList<>();

        if (hasText(title)) {
            filtrosInformados.add(titleContains(title));
        }
        if (hasText(city)) {
            filtrosInformados.add(cityEquals(city));
        }
        if (minPrice != null) {
            filtrosInformados.add(priceGreaterThanOrEqual(minPrice));
        }
        if (maxPrice != null) {
            filtrosInformados.add(priceLessThanOrEqual(maxPrice));
        }
        if (minBedrooms != null) {
            filtrosInformados.add(bedroomsGreaterThanOrEqual(minBedrooms));
        }
        if (minArea != null) {
            filtrosInformados.add(areaGreaterThanOrEqual(minArea));
        }

        Specification<Property> combinada = (root, query, builder) -> builder.conjunction();
        for (Specification<Property> filtro : filtrosInformados) {
            combinada = combinada.and(filtro);
        }
        return combinada;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}