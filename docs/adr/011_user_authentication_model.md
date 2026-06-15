# 011 - User Authentication Model

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
The backend needs to protect `/api/**` routes while allowing clients to authenticate once and then call protected endpoints without server-side sessions. User credentials and roles also need to live in the database instead of being hardcoded in the security configuration. The frontend should be able to build on this with a login view, token storage, and an authenticated API adapter.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Persist users and roles and authenticate requests with stateless JWT bearer tokens
2. Option - Use HTTP Basic authentication for every protected request
3. Option - Keep in-memory users in the Spring Security configuration

## Decision
We decided to use **persistent users with stateless JWT bearer authentication** because it separates user data from configuration and fits a REST API consumed by a frontend client. Login requests are authenticated through Spring Security, passwords are verified through BCrypt, successful logins return a bearer token, and protected API requests are authenticated by the JWT filter. This prepares the backend contract for a frontend auth layer that sends `Authorization: Bearer <token>` on protected API requests.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
The application now depends on a correctly configured JWT secret and token expiration. User roles are stored with the user and mapped to Spring Security authorities, which makes authorization extensible but requires database-backed user management to remain consistent with the security model. The frontend still needs to implement the visible auth flow, including login state, logout, route guards, and handling `401 Unauthorized` responses.
