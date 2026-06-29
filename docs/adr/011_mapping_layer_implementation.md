# 011 - Mapping Layer Implementation (MapStruct)

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted | refines ADR-008
* **Date:** 2026-06-29

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
[ADR-008](008_introduce_mapping_layer.md) proposed introducing a mapping layer with MapStruct, but left the concrete realization open. This ADR records how it was actually implemented and which additional decisions became necessary during implementation.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - One MapStruct mapper per aggregate in a dedicated `mapper` package, services only delegate
2. Option - A single central mapper for all aggregates
3. Option - MapStruct only for trivial mappings, keep hand-written mappers for the rest

## Decision
We implemented **Option 1**: a dedicated `hsd.inflab.smp.mapper` package with one MapStruct mapper per aggregate (`RecipeMapper`, `PantryItemMapper`, `DailyMealMapper`, all `componentModel = "spring"`). All `convertTo*` methods were removed from the services.

Decisions made during implementation (beyond ADR-008):
* **Annotation processor order** in `pom.xml` is fixed because the entities use Lombok: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`.
* **`DailyMealMapper` uses `injectionStrategy = CONSTRUCTOR`** so the generated implementation can be instantiated in plain unit tests (`new DailyMealMapperImpl(new RecipeMapperImpl())`).
* **Not everything is MapStruct.** `RecipeApiMapper` (external API DTO → recipe DTO, incl. unit lookup) remains a hand-written `@Component` and was moved into the `mapper` package. The recipe-reference resolution and the upsert-by-date logic stay in `DailyMealService` — they are business logic, not mapping.
* **Architecture enforcement** via a new ArchUnit test (`MapperRulesTest`): mappers may only access `dto`, `entity`, `enums`, `util`; they must not access `repository` or `controller`.

## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
* Services contain business logic only; entity/DTO conversion lives in one place.
* Implementations are generated at compile time; the generated `*Impl` classes are not in the source tree (visible under `target/generated-sources`).
* New aggregates follow the same pattern: add a mapper interface, inject it into the service.
* Trade-off: an additional build-time dependency (MapStruct) and a strict processor ordering that must be preserved when touching `pom.xml`.
* Supersedes the open points of ADR-008; ADR-008 itself remains unchanged as the original proposal.
