package br.com.fiap.imovelfacil.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Sem isso a coleção sairia como _embedded.propertyList; o contrato pede _embedded.properties
@Relation(collectionRelation = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private PropertyType type;

    private String city;
    private String neighborhood;

    // aluguel mensal, em reais
    private BigDecimal price;

    // Integer (e não int) para que um filtro não informado seja null, e não 0
    private Integer bedrooms;

    // em m²
    private Double area;
}