# 012 - Request & Response DTO Implementation

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted | refines ADR-007, builds on ADR-011
* **Date:** 2026-06-29

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
[ADR-007](007_request_and_response_dtos.md) decided to split the shared DTOs into request and response DTOs, but two concrete questions were left open: how to reference other aggregates from a request DTO, and how to organize/name the resulting DTOs. This ADR records those decisions and the realized implementation.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Request DTOs reference other aggregates by **id only** (e.g. `breakfastRecipeId`)
2. Option - Request DTOs **nest** the full request DTO of the referenced aggregate
3. Option - Keep a single DTO and validate that `id` is null on write

For the package/naming question:
1. Option - Split into `dto.request` / `dto.response` subpackages and rename response DTOs to `…ResponseDto`
2. Option - Keep all DTOs flat in `dto` and distinguish only by class name

## Decision
**Reference style:** We chose **id-only references** (Option 1). `DailyMealRequestDto` references recipes via `breakfastRecipeId` / `lunchRecipeId` / `dinnerRecipeId` (UUID). This matches the existing server logic, which only ever used the recipe id, keeps the contract unambiguous ("assign an existing recipe") and avoids ignored fields. Nesting (Option 2) would have re-introduced the orphaned-field problem ADR-007 set out to remove.

**Package & naming:** We split the DTOs into `dto.request`, `dto.response` and the unchanged `dto.external`. Response DTOs carry the `…ResponseDto` suffix (`RecipeResponseDto`, `RecipeIngredientResponseDto`, `PantryItemResponseDto`, `DailyMealResponseDto`) so the type name reflects its role; request DTOs use `…RequestDto`. `LoginRequest` / `LoginResponse` were already role-named and only moved into the matching package.

## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
* The API contract is honest: request payloads cannot carry an `id` at all (structurally impossible, not just filtered).
* Controllers accept request DTOs and return response DTOs; the mappers (see ADR-011) gained `request → entity` methods without `id`.
* ArchUnit rules referencing the DTO package were widened from `hsd.inflab.smp.dto` to `hsd.inflab.smp.dto..` to cover the new subpackages (and now also `dto.external`).
* If inline creation of a referenced entity is ever needed (e.g. create a recipe while saving a meal plan), it can be added later without breaking the id-reference contract.
* Trade-off: more DTO classes and packages — accepted in line with ADR-007's stated consequences.
* ADR-007 remains unchanged as the original proposal; this ADR documents its realization.
