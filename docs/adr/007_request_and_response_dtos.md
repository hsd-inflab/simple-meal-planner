# 007 - Implement Request & Response DTOs

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted (originally Proposed)
* **Date:** 12.05.2026 · updated 30.06.2026

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
POST and GET commands are currently utilizing the same DTOs in the controller. this is an issue, because although the backend filters out a possible id set by the frontend, this happens implicitly and might result in inconsistencies. 
## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - implement RequestDTOs and ResponseDTOs
2. Option - throw an exception when id is not null
3. Option - do nothing and hope for the best

## Decision
I suggest we implement **Option 1** because this offers the clearest solution to our problem. there are no more misplaced or orphaned fields in the corresponding DTOs.

## Implementation (30.06.2026)
<!-- Realization of the original proposal; folded in from the former ADR-012. -->
When realizing Option 1, two concrete questions were left open by the original proposal: how to reference other aggregates from a request DTO, and how to organize/name the resulting DTOs.

**Reference style:** We chose **id-only references**. `DailyMealRequestDto` references recipes via `breakfastRecipeId` / `lunchRecipeId` / `dinnerRecipeId` (UUID). This matches the existing server logic, which only ever used the recipe id, keeps the contract unambiguous ("assign an existing recipe") and avoids ignored fields. Nesting the full request DTO of the referenced aggregate would have re-introduced the orphaned-field problem this ADR set out to remove.

**Package & naming:** We split the DTOs into `dto.request`, `dto.response` and the unchanged `dto.external`. Response DTOs carry the `…ResponseDto` suffix (`RecipeResponseDto`, `RecipeIngredientResponseDto`, `PantryItemResponseDto`, `DailyMealResponseDto`) so the type name reflects its role; request DTOs use `…RequestDto`. `LoginRequest` / `LoginResponse` were already role-named and only moved into the matching package.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
more abstraction, more boilerplate. decreases understandability of the codebase for new maintainers.

increases codebase stability and logic coherence.

Realized consequences:
* The API contract is honest: request payloads cannot carry an `id` at all (structurally impossible, not just filtered).
* Controllers accept request DTOs and return response DTOs; the mappers (see ADR-008) gained `request → entity` methods without `id`.
* ArchUnit rules referencing the DTO package were widened from `hsd.inflab.smp.dto` to `hsd.inflab.smp.dto..` to cover the new subpackages (and now also `dto.external`).
* If inline creation of a referenced entity is ever needed (e.g. create a recipe while saving a meal plan), it can be added later without breaking the id-reference contract.
