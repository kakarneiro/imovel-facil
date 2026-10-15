package br.com.fiap.imovelfacil.repository;

import br.com.fiap.imovelfacil.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaRepository já traz a paginação (findAll(Pageable)) e o Query by Example
 * (findAll(Example, Pageable)).
 *
 * JpaSpecificationExecutor é o que habilita a busca por Specification,
 * usada no /properties/search para faixas de valor e "contém".
 */
public interface PropertyRepository extends JpaRepository<Property, Long>,
        JpaSpecificationExecutor<Property> {
}