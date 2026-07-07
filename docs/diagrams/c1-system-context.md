# C1 – System Context

Shows the Simple Meal Planner backend as a single box, the people/clients that use
it, and the external systems it depends on.

Dashed elements are planned or not yet wired up (see [README legend](README.md#legend)).

```mermaid
graph TB
    user["User<br/><i>[Person]</i><br/>Plans meals, manages pantry and recipes"]
    client["Flutter Frontend<br/><i>[Software System — separate repository]</i><br/>Meal planner UI"]
    tools["API Tools<br/><i>[Software System]</i><br/>Browser / Postman for testing"]

    subgraph smp["Simple Meal Planner"]
        backend["Meal Planner Backend<br/><i>[Software System]</i><br/>Manages pantry, recipes and meal plans;<br/>exposes a JWT-secured REST API"]
    end

    db[("PostgreSQL<br/><i>[External System]</i><br/>Stores users, pantry,<br/>recipes and meal plans")]
    recipeApi["External Recipe API (RapidAPI)<br/><i>[External System]</i><br/>Recipe search &amp; crawling"]

    user --> client
    user --> tools
    client -->|"HTTPS / JSON<br/>REST /api/**"| backend
    tools -->|"HTTP / JSON<br/>REST /api/**"| backend
    backend -->|"JDBC / SQL"| db
    backend -.->|"HTTPS / JSON<br/>(implemented, not yet exposed)"| recipeApi

    classDef planned stroke-dasharray: 5 5;
    class recipeApi planned;
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
