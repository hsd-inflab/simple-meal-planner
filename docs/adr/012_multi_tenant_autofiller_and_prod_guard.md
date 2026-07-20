# 012 - Multi-Tenant Database Autofiller with Production Guard

* **Status:** Proposed
* **Date:** 2026-07-20

## Context and Problem Statement
The `DatabaseAutofillerService` seeds an empty database with demo data for development. Today it runs as an unconditional `CommandLineRunner` in every profile and checks whether the business tables are globally empty. With the switch to multi-tenancy this no longer works: seed data must belong to a dedicated test user, real user data must never block or be touched by the seeding, and the autofiller must never run in production. In the future there will be two database containers, one for test and one for prod.

## Considered Options

### How to prevent the autofiller from running in production
1. Option - Opt-in property (`app.autofill.enabled=false` by default, `@ConditionalOnProperty`) **combined with** `@Profile("!prod")`
2. Option - `@Profile("!prod")` alone — rejected: fails open, an unknown or new profile would still run the autofiller
3. Option - Opt-in property alone — safe by default, but a single misconfigured flag in prod would be enough

### How to make the seeding multi-tenant
1. Option - Seed data is owned by a configured test user (`app.autofill.username`); the emptiness check becomes "does the test user already have data?" via repository queries
2. Option - Keep the global table-emptiness check — rejected: real user data would block seeding, and seeded data would have no owner

### How to run two database containers (test / prod)
1. Option - Compose base + override file: `compose.yaml` stays the dev/test setup (autofill enabled), a new `compose.prod.yaml` defines the prod Postgres with its own volume, own credentials (`PROD_DB_*`), no host port mapping, `SPRING_PROFILES_ACTIVE=docker,prod`, and no autofill flag
2. Option - One compose file with Docker Compose profiles (`--profile test` / `--profile prod`) — preferable only if both containers must run on the same host at the same time

## Decision
We decided to use **the fail-closed combination (option 1) in all three areas**:

1. The autofiller is disabled by default and only activated by an explicit `app.autofill.enabled=true`; additionally the bean is excluded from the `prod` profile. Any new environment that configures nothing gets no autofill. This mirrors the existing `app.security.seed-default-user` pattern in `DataLoader`.
2. All seed data (pantry items, recipes, daily meals) is owned by a configured test user, and seeding is skipped only if that user already has data. Data of other users is never read or modified.
3. Test and prod databases are separate containers with separate volumes and credentials, defined in `compose.yaml` (test) and a new `compose.prod.yaml` (prod). The datasource stays environment-variable driven; the `prod` Spring profile only controls behavior.

## Open Questions
These need a team decision before implementation:

1. **Who creates the test user?** (a) Merge `DataLoader` and the autofiller into one `TestDataSeeder` that creates the user and then its demo data — recommended, makes the dependency explicit; or (b) keep two runners ordered with `@Order`, where the autofiller requires the seed user to exist.
2. **If the runners stay separate (1b): what happens when the test user is missing?** Fail the startup, or log a warning and skip seeding? (Irrelevant with 1a.)
3. **Compose layout:** do test and prod containers ever run on the same host at the same time? If yes, prefer Docker Compose profiles in one file over the base + override split.
4. **Test user identity:** fixed username (`testuser`) or fully configurable? The password must come from an environment variable, never from the repository.

## Consequences
- Enabling demo data becomes an explicit action per environment; forgetting to configure something can no longer seed a production database (fails closed twice).
- The autofiller gains a dependency on the user model; the entities need an owner reference (FK plus Flyway migration) first — that is a separate, prerequisite task.
- The `JdbcTemplate`-based raw table checks are replaced by repository queries, which keeps the autofiller aligned with the entity model.
- Prod deployments get their own compose file, volume, and credentials, so test data and prod data can never share a database instance.

## Implementation Order
1. Add the `app.autofill.enabled` flag (default `false`) with `@ConditionalOnProperty`.
2. Introduce the `prod` profile and add `@Profile("!prod")`.
3. Convert the autofiller to test-user-owned seeding with a per-user emptiness check.
4. Split compose into test and prod setups with separate volumes and credentials.
