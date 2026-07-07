# C1 – System Context

Shows the Simple Meal Planner backend as a single box, the people/clients that use
it, and the external systems it depends on.

Dashed elements are planned or not yet wired up (see [README legend](README.md#legend)).

```mermaid
graph TB
    user["User<br/><i>[Person]</i><br/>Plans meals, manages pantry and recipes"]
    client["API Client<br/><i>[Software System]</i><br/>Browser / Postman today,<br/>Flutter app planned"]

    subgraph smp["Simple Meal Planner"]
        backend["Meal Planner Backend<br/><i>[Software System]</i><br/>Manages pantry, recipes and meal plans;<br/>exposes a JWT-secured REST API"]
    end

    db[("PostgreSQL<br/><i>[External System]</i><br/>Stores users, pantry,<br/>recipes and meal plans")]
    recipeApi["External Recipe API (RapidAPI)<br/><i>[External System]</i><br/>Recipe search &amp; crawling"]

    user --> client
    client -->|"HTTPS / JSON<br/>REST /api/**"| backend
    backend -->|"JDBC / SQL"| db
    backend -.->|"HTTPS / JSON<br/>(implemented, not yet exposed)"| recipeApi

    classDef planned stroke-dasharray: 5 5;
    class recipeApi planned;
```

## Elements

| Element | Type | Notes |
|---------|------|-------|
| User | Person | Interacts through an API client. |
| API Client | Software System | Currently browser/Postman; a Flutter frontend is planned ([ADR 001](../adr/001_use_flutter_as_frontend.md)) but not present in this repo. |
| Meal Planner Backend | Software System | Spring Boot application, all endpoints under `/api/**`; `/api/auth/**` is public, the rest require a JWT. |
| PostgreSQL | External System | Relational persistence; schema managed by Flyway. |
| External Recipe API | External System | Reached via `RecipeApiClient`; the client and `RecipeAPIService` exist but are **not called by any controller yet** — drawn dashed. |
