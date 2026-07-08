# C1 – System Context

Shows the Simple Meal Planner backend as a single box, the people/clients that use
it, and the external systems it depends on.

Dashed arrows / `[not yet wired up]` labels mark parts that are implemented but not yet
exposed (see [README legend](README.md#legend)).

```mermaid
graph TB
    user["User<br/>(Person)<br/>Plans meals, manages pantry and recipes"]
    client["Flutter Frontend<br/>(Software System - separate repository)<br/>Meal planner UI"]
    tools["API Tools<br/>(Software System)<br/>Browser / Postman for testing"]

    subgraph smp["Simple Meal Planner"]
        backend["Meal Planner Backend<br/>(Software System)<br/>Manages pantry, recipes and meal plans.<br/>Exposes a JWT-secured REST API"]
    end

    db[("PostgreSQL<br/>(External System)<br/>Stores users, pantry, recipes, meal plans")]
    recipeApi["External Recipe API / RapidAPI<br/>(External System - not yet wired up)<br/>Recipe search and crawling"]

    user --> client
    user --> tools
    client -->|"HTTPS / JSON REST /api"| backend
    tools -->|"HTTP / JSON REST /api"| backend
    backend -->|"JDBC / SQL"| db
    backend -.->|"HTTPS / JSON (not yet exposed)"| recipeApi
```

## Elements

| Element | Type | Notes |
|---------|------|-------|
| User | Person | Interacts through the Flutter frontend (or API tools). |
| Flutter Frontend | Software System | The meal planner UI ([ADR 001](../adr/001_use_flutter_as_frontend.md)). Exists and is maintained in a **separate repository**; consumes this backend's REST API. |
| API Tools | Software System | Browser/Postman used to exercise the API directly during development. |
| Meal Planner Backend | Software System | Spring Boot application, all endpoints under `/api/**`; `/api/auth/**` is public, the rest require a JWT. |
| PostgreSQL | External System | Relational persistence; schema managed by Flyway. |
| External Recipe API | External System | Reached via `RecipeApiClient`; the client and `RecipeAPIService` exist but are **not called by any controller yet** — drawn dashed. |
