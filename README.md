# Imóvel Fácil API

API REST de catálogo de imóveis para locação, com busca, paginação, HATEOAS, cache e documentação.

Projeto Diamante — Java Advanced (FIAP).

## Integrantes

| Nome | RM |
|---|---|
| Matheus Carneiro Maciel | 567753 |
| Murilo Marques | 568224 |
| Paulo Henrique da Silva Kian | 563343 |

## Tecnologias

- Java 25 e Spring Boot 4.1.1
- Spring Web, Spring Data JPA e Bean Validation
- Spring HATEOAS, Spring Cache e Spring Boot Actuator
- springdoc-openapi 3.1.1 (Swagger UI)
- Banco H2 em memória

## Como executar

O projeto sobe sem nenhuma configuração extra.

```bash
mvn spring-boot:run
```

Ou abra o projeto na IDE e execute a classe `ImovelFacilApplication`.

A API fica em `http://localhost:8080`. O banco é em memória e já sobe com 60 imóveis
carregados pelo `DataLoader`, recriados a cada inicialização.

| Recurso | Endereço |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator | http://localhost:8080/actuator/health, `/info`, `/caches` |

## Endpoints

### Listagem paginada

`GET /properties` → **200**

Sem parâmetros devolve a página 0 com 10 imóveis, ordenados por id.

```bash
curl "http://localhost:8080/properties"
curl "http://localhost:8080/properties?page=1&size=5"
```

### Busca por Query by Example

`GET /properties/example` → **200**

Filtros exatos e opcionais: `type`, `city`, `neighborhood` e `bedrooms`.
Textos ignoram maiúsculas e minúsculas; filtros não informados são ignorados.

```bash
curl "http://localhost:8080/properties/example?type=APARTMENT&city=são%20paulo&bedrooms=2"
```

### Busca por Specification

`GET /properties/search` → **200**

A busca que o Example não faz: faixas de valor e "contém".

| Parâmetro | Regra |
|---|---|
| `title` | contém, sem caixa |
| `city` | igual, sem caixa |
| `minPrice` | price ≥ valor |
| `maxPrice` | price ≤ valor |
| `minBedrooms` | bedrooms ≥ valor |
| `minArea` | area ≥ valor |

Todos são opcionais e combinam entre si.

```bash
curl "http://localhost:8080/properties/search?title=apartamento&minPrice=3000&maxPrice=6000&minBedrooms=2"
```

### Detalhe de um imóvel

`GET /properties/{id}` → **200** · **404**

```bash
curl "http://localhost:8080/properties/7"
```

### Cadastro

`POST /properties` → **201** (com header `Location`) · **400**

```bash
curl -X POST http://localhost:8080/properties \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Apartamento com varanda",
    "description": "Perto do metrô",
    "type": "APARTMENT",
    "city": "São Paulo",
    "neighborhood": "Pinheiros",
    "price": 3200.00,
    "bedrooms": 2,
    "area": 68.5
  }'
```

### Atualização

`PUT /properties/{id}` → **200** · **400** · **404**

```bash
curl -X PUT http://localhost:8080/properties/7 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Apartamento reformado",
    "description": "Perto do metrô",
    "type": "APARTMENT",
    "city": "São Paulo",
    "neighborhood": "Pinheiros",
    "price": 3500.00,
    "bedrooms": 2,
    "area": 68.5
  }'
```

### Remoção

`DELETE /properties/{id}` → **204** · **404**

```bash
curl -X DELETE http://localhost:8080/properties/7
```

## Formatos de resposta

### Um imóvel (EntityModel)

```json
{
  "id": 7,
  "title": "Apartamento com varanda",
  "description": "Perto do metrô",
  "type": "APARTMENT",
  "city": "São Paulo",
  "neighborhood": "Pinheiros",
  "price": 3200.00,
  "bedrooms": 2,
  "area": 68.5,
  "_links": {
    "self": { "href": "http://localhost:8080/properties/7" },
    "properties": { "href": "http://localhost:8080/properties" }
  }
}
```

### Uma página (PagedModel)

```json
{
  "_embedded": { "properties": [ ... ] },
  "_links": { "first": {}, "prev": {}, "self": {}, "next": {}, "last": {} },
  "page": { "size": 10, "totalElements": 60, "totalPages": 6, "number": 0 }
}
```

Os links de navegação preservam os filtros da busca. Uma página vazia não traz `_embedded`.

### Erros

Todos os erros saem no mesmo formato, sem expor stack trace:

```json
{ "error": "Property not found" }
```

| Situação | Status |
|---|---|
| Dados de entrada inválidos | 400 |
| JSON malformado ou valor de enum inexistente | 400 |
| Imóvel inexistente | 404 |

## Cache

O cache `properties` guarda o detalhe do imóvel:

- `@Cacheable` no `GET /properties/{id}` — a segunda leitura do mesmo imóvel não vai ao banco
- `@CachePut` no `PUT` — atualiza a entrada em vez de invalidá-la
- `@CacheEvict` no `DELETE` — remove a entrada, e o próximo `GET` responde 404

Para observar: o `PropertyService` registra `🔎 Fetching property {id} from the database`
sempre que o banco é realmente consultado. A linha aparece na primeira leitura e some na segunda.

## Decisões de implementação

**Specification combinada a partir de uma condição neutra.** Cada filtro do `/search` é uma
Specification isolada; o método `search()` combina com `and` apenas os que vieram preenchidos,
partindo de uma `conjunction()`. Assim uma busca sem nenhum filtro devolve todos os imóveis
em vez de quebrar.

**`@Relation(collectionRelation = "properties")` na entidade.** Sem essa anotação o Spring HATEOAS
nomearia a coleção como `_embedded.propertyList`, fora do contrato esperado pelo app.

**`Location` do POST reaproveita o link `self`.** O assembler já monta a URI do imóvel criado,
então não faz sentido remontá-la à mão no controller.

**CORS separado para o Actuator.** Os endpoints do Actuator não passam pelo `WebConfig`
(que configura o CORS do Spring MVC) — eles têm configuração própria, definida em
`management.endpoints.web.cors` no `application.yml`.

## Observação sobre o uso do cache

Como o próprio enunciado aponta, cache no detalhe de um catálogo de imóveis não é a aplicação
mais adequada, já que a lista muda com frequência. A implementação segue o pedido por ser
um exercício didático sobre as anotações de cache do Spring.
