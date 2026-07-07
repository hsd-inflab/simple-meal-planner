# 011 - Random User Registration

* **Status:** Accepted
* **Date:** 2026-07-05

## Context and Problem Statement
The frontend needs a simple way to create a user account without implementing username generation, password generation, uniqueness checks, or credential persistence rules itself. The generated username must follow the accepted adjective-noun-digits format, the password must be generated securely, and the assigned user must be stored consistently with the existing JWT authentication model.

## Considered Options
1. Option - Provide a backend endpoint that creates a random user, stores the password hash, and returns the generated credentials with a bearer token
2. Option - Let the frontend generate usernames and passwords, then submit them to the backend
3. Option - Require manually chosen usernames and passwords during registration

## Decision
We decided to use **a backend-owned random registration endpoint** because the backend can enforce the username format, reuse the shared credential vocabulary, check database uniqueness, hash the generated password, and issue a JWT in one consistent flow. Clients create an account through `POST /api/auth/register/random`; the response contains the generated username, the generated password once in clear text, and a bearer token for immediate authenticated API access.

## Consequences
The frontend does not need to duplicate credential generation rules and can offer a simple "create access" flow. The clear-text password is only returned at registration time; the database stores only the encoded password. Registration is currently public because `/api/auth/**` is permitted, so account creation should be revisited if the application later needs invite-only registration, rate limiting, or administrative user provisioning. User deletion is intentionally not part of this decision because the domain data is not yet user-scoped.

For local Docker-based verification, the backend image must be rebuilt from a current application jar because the existing `Dockerfile` copies `target/*.jar` into the image. The verified flow is:

1. Stop existing Compose services with `docker compose down`.
2. Build the current jar with `.\mvnw.cmd clean package -DskipTests`.
3. Rebuild and start the Docker services with `docker compose up -d --build`.
4. Create a random user with `POST http://localhost:8080/api/auth/register/random`.
5. Use the returned bearer token for protected endpoints, e.g. `GET http://localhost:8080/api/pantry` with `Authorization: Bearer <token>`.

If the registration endpoint returns `404 Not Found` during local testing, the running backend image is likely stale and was built before the endpoint existed. Rebuilding the jar before rebuilding the Compose services ensures the container runs the current backend code.
