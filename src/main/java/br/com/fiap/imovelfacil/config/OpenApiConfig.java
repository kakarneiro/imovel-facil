package br.com.fiap.imovelfacil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Título, versão e descrição que aparecem no topo do Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI imovelFacilOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Imóvel Fácil API")
                        .version("1.0.0")
                        .description("""
                                API de catálogo de imóveis para locação.

                                Oferece listagem paginada, busca por Query by Example \
                                (filtros exatos) e busca por Specification (faixas de valor \
                                e trechos de título), com links de navegação HATEOAS em todas \
                                as respostas."""));
    }
}