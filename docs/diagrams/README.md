# Architecture Diagrams

Mermaid diagrams describing the Simple Meal Planner backend, structured after the
[C4 model](https://c4model.com/) (Context, Container, Component) plus two
supplementary diagrams (data model and a key runtime sequence).

The diagrams reflect the **current state of the code**, not the long-term target.
Where something is planned or implemented but not yet wired up, it is drawn with a
**dashed line/border** and labelled accordingly.

## Contents

| File | C4 level | Purpose |
|------|----------|---------|
| [c1-system-context.md](c1-system-context.md) | Level 1 – Context | Who uses the system and which external systems it talks to. |
| [c2-container.md](c2-container.md) | Level 2 – Container | Deployable units (backend, database, migrations) and their protocols. |
| [c3-component-backend.md](c3-component-backend.md) | Level 3 – Component | Internal layered structure of the Spring Boot backend. |
| [data-model-er.md](data-model-er.md) | supplementary | Relational schema created by the Flyway migrations. |
| [sequence-auth-jwt.md](sequence-auth-jwt.md) | supplementary | Login and JWT-protected request flow. |

## Legend

- **Solid arrow** — implemented and active in the current codebase.
- **Dashed arrow** + a `[not yet wired up]` note in the node label — implemented but not
  yet exposed (currently: the external recipe API integration `RecipeApiClient` /
  `RecipeAPIService`, which exists but is not called by any controller yet).

## Notes on the current state

- This repository contains **the backend only**. The Flutter frontend
  ([ADR 001](../adr/001_use_flutter_as_frontend.md)) exists in a **separate repository**
  and consumes this backend's REST API.
- Architecture layers follow [ADR 004 (layered architecture)](../adr/004_layered_architecture.md),
  [ADR 007 (request/response DTOs)](../adr/007_request_and_response_dtos.md) and
  [ADR 008 (mapping layer)](../adr/008_introduce_mapping_layer.md).
- Authentication follows [ADR 010 (user authentication model)](../adr/010_user_authentication_model.md).

## Maintaining these diagrams

When the code changes, update the affected diagram in the same pull request.
The data-model diagram is derived from `src/main/resources/db/migration/` — keep it
in sync when a new migration is added.
