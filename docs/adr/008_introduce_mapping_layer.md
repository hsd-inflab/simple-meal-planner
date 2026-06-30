# 008 - Introduce Mapping Layer

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted (originally Proposed) | connected to ADR-007
* **Date:** 12.05.2026 · updated 30.06.2026

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
DTOs and entities need to be converted into one another, this currently happens within the corresponding service. This is a deliberate design decision. But mapping is not a core responsibility of a service, which also violates SRP. A growing number of DTOs increased the amount of mapping code in services to the point where they are more mapping than performing business logic.
## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Implement a Mapping Layer with MapStruct
2. Option - Implement Mapping without MapStruct
3. Option - keep as is

## Decision
I propose we use **Option 1** because it drastically reduces boilerplate. Apart from the mapper instance, there is only an annotation needed - given that both dto and entity match in field name, count and type. 

## Implementation (30.06.2026)
<!-- Realization of the original proposal; folded in from the former ADR-011. -->
The original proposal left the concrete realization open. It was implemented as a dedicated `hsd.inflab.smp.mapper` package with one MapStruct mapper per aggregate (`RecipeMapper`, `PantryItemMapper`, `DailyMealMapper`, all `componentModel = "spring"`). All `convertTo*` methods were removed from the services.

Decisions made during implementation (beyond the original proposal):
* **Annotation processor order** in `pom.xml` is fixed because the entities use Lombok: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`.
* **`DailyMealMapper` uses `injectionStrategy = CONSTRUCTOR`** so the generated implementation can be instantiated in plain unit tests (`new DailyMealMapperImpl(new RecipeMapperImpl())`).
* **Not everything is MapStruct.** `RecipeApiMapper` (external API DTO → recipe DTO, incl. unit lookup) remains a hand-written `@Component` and was moved into the `mapper` package. The recipe-reference resolution and the upsert-by-date logic stay in `DailyMealService` — they are business logic, not mapping.
* **Architecture enforcement** via a new ArchUnit test (`MapperRulesTest`): mappers may only access `dto`, `entity`, `enums`, `util`; they must not access `repository` or `controller`.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
* Services contain business logic only; entity/DTO conversion lives in one place.
* Implementations are generated at compile time; the generated `*Impl` classes are not in the source tree (visible under `target/generated-sources`).
* New aggregates follow the same pattern: add a mapper interface, inject it into the service.
* Trade-off: an additional build-time dependency (MapStruct) and a strict processor ordering that must be preserved when touching `pom.xml`.
