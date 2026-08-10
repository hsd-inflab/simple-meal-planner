# 013 - Multi-Tenant Database Autofiller with Production Guard

* **Status:** Accepted
* **Date:** 2026-08-10

## Context and Problem Statement
The `DatabaseAutofillerService` seeds demo data for development. It ran as an unconditional `CommandLineRunner` in every profile, so any deployment would have written demo data into its own database. With the move to multi-tenancy the seed data additionally needs an owner, and test and production must never share a database instance.

## Considered Options

### Preventing the autofiller from running in production
1. Option - Opt-in property (`app.autofill.enabled`, default `false`) via `@ConditionalOnProperty` **combined with** `@Profile("!prod")`
2. Option - `@Profile("!prod")` alone - fails open, because an unknown or newly introduced profile would still run the autofiller
3. Option - Opt-in property alone - safe by default, but a single misconfigured environment variable in production is enough

### Separating the test and production databases
1. Option - `compose.yaml` for test plus a standalone `compose.prod.yaml` with its own volume, its own `PROD_DB_*` credentials and its own compose project name
2. Option - One compose file using Docker Compose profiles - only preferable if both stacks have to run on the same host at the same time

## Decision
We decided to use **option 1 in both areas**.

The autofiller is disabled by default and is only activated by an explicit `app.autofill.enabled=true`; in addition the bean is excluded from the `prod` profile. Two independent mistakes are now required before demo data can reach a production database. This mirrors the existing `app.security.seed-default-user` pattern in `DataLoader`.

Test and production run as separate compose stacks with separate volumes and credentials. The datasource stays environment-variable driven, so the `prod` profile only controls behavior.

## Consequences
- Enabling demo data becomes an explicit action per environment. An environment that configures nothing gets no autofill.
- Local development and the docker test stack switch the flag on deliberately, via `application-local.properties` and `APP_AUTOFILL_ENABLED=true` in `compose.yaml`.
- `@Profile("!prod")` matches the exact string `prod`. A deployment activating `production` or `prd` loses the second barrier and relies on the property alone.
- The guard only protects what is actually deployed. `Dockerfile` copies `target/*.jar` without rebuilding it, so an outdated jar can ship without the guard - this was observed during verification. Producing the artifact from the current source is a separate, still open task.
- `DataLoader` keeps only its property guard and has no profile guard, so a single environment variable can still seed a default user in production. Aligning it with the autofiller is a separate task.
- The production Postgres is published on `127.0.0.1:5433` for local inspection. On a deployed host this port mapping has to be removed.
- The multi-tenant part of this decision is **not** implemented: seed data still has no owner and the emptiness check is still global. It requires an owner reference on `PantryItem`, `Recipe` and `DailyMeal` (foreign key plus Flyway migration) and a decision on whether `DataLoader` and the autofiller are merged into a single seeder.
