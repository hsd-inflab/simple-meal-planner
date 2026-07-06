# 011 - Expose enum display labels via metadata API

* **Status:** Accepted
* **Date:** 06.07.2026

## Context and Problem Statement
Units and categories are serialized as raw enum names (e.g. `"MEAT"`, `"ML"`) in the response DTOs, while their German labels only exist in the backend `*_de.properties` bundles. The frontend therefore has no way to render human-readable strings. How should the frontend learn the enum-to-label mapping?

## Considered Options
1. Option - Add a dedicated read-only metadata endpoint that delivers the enum-to-label mapping for units and categories.
2. Option - Inline a `displayName` next to every enum field in each response DTO (e.g. `unit` + `unitDisplayName`).
3. Option - Hardcode a copy of the translation table in the frontend.

## Decision
We decided to use **Option 1**. The backend exposes `GET /api/metadata` (plus the convenience endpoints `GET /api/metadata/units` and `GET /api/metadata/categories`), each returning a list of `{ value, displayName }` objects. The frontend fetches this once on startup, caches it, and looks up the label for a raw enum value when rendering. This keeps the `*_de.properties` bundles as the single source of truth and avoids bloating every response payload (Option 2) or duplicating the mapping (Option 3).

## Consequences
* The raw enum name stays the stable lookup key across all existing endpoints; response DTOs (`PantryItemResponseDto`, `RecipeIngredientResponseDto`, …) are unchanged.
* The mapping has one owner: the backend `Unit`/`Category` enums plus their `*_de.properties` bundles. Adding a new enum value requires updating the properties file, nothing in the frontend.
* The response list preserves enum declaration order, so it can drive stable dropdown ordering in the frontend.
* The endpoint lives under `/api/**` and is therefore authenticated like every other API endpoint (see ADR-010); it is not exposed publicly.
* New layer members follow the existing layered architecture (ADR-004): `MetadataController` → `MetadataService`, returning `MetadataResponseDto` / `EnumOptionDto` records (ADR-005, ADR-007).
* The locale is currently fixed to German because only `*_de.properties` bundles exist. Supporting more languages later would mean reading `Accept-Language` and adding further bundles.
* Follow-up: the correct UTF-8 encoding of umlaut labels (e.g. `Gemüse`, `Gewürze`) in the `.properties` bundles should be verified against the running endpoint, since the automated tests only assert ASCII labels.
* Acceptance criteria "fetch & cache" and "render" of the originating user story are implemented in the separate Flutter frontend repository (ADR-001), not here; this ADR covers only the backend API that enables them.
