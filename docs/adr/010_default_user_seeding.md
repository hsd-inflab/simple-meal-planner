# 010 - Default User Seeding

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
* **Date:** 2026-06-15

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
Local development and integration tests need a predictable user account for authentication without requiring manual database setup. At the same time, automatic user creation must not silently create default credentials in environments where this is not intended.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Seed a configurable default user only when explicitly enabled
2. Option - Always create a default user on application startup
3. Option - Require every developer and test setup to create users manually

## Decision
We decided to use **explicitly enabled default user seeding** because it keeps local and test authentication reproducible while keeping default credentials disabled by default. The seed user is created through `DataLoader`, receives a BCrypt-hashed password, and is skipped when a user with the configured username already exists.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
Environments that need a seed user must set `app.security.seed-default-user.enabled=true` and provide `app.security.user.name` and `app.security.user.password`. This adds configuration work, but avoids accidental default users in normal application startup.
