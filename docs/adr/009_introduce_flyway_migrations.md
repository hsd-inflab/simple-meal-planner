# 009 - Introduce Flyway Migrations

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted
* **Date:** 2026-06-09

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
The database schema needs to be created and evolved in a controlled, repeatable way. Relying on manual SQL execution or Hibernate schema generation makes it harder to reproduce environments, review schema changes, and keep local, test, and Docker setups aligned.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Use Flyway migrations as the single source of truth for schema changes
2. Option - Manage schema changes manually with ad-hoc SQL scripts
3. Option - Let Hibernate create or update the schema automatically

## Decision
We decided to use **Flyway migrations** because they provide versioned, repeatable database changes that can be executed consistently across environments. The migrations are stored in `src/main/resources/db/migration`, and schema validation remains enabled so the application fails fast when the database does not match the expected structure.

## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
Schema changes now require a new migration file instead of direct table changes. This adds some discipline and extra files, but it improves traceability, supports reproducible deployments, and reduces the risk of schema drift between local development and Docker-based environments.

