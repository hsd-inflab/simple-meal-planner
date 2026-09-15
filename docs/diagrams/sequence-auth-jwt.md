# Sequence – Authentication, tenant authorization & protected request

Shows the two core runtime flows of the security layer and the tenant-authorization seam
([ADR 010](../adr/010_user_authentication_model.md)):

1. Login: exchange credentials for a JWT.
2. Access a protected endpoint using that JWT and restrict data access to the authenticated tenant.

`SecurityConfig` makes `/api/auth/**` public and requires authentication for all other
`/api/**`; sessions are stateless.

## 1. Login

```mermaid
sequenceDiagram
    actor Client
    participant AC as AuthController<br/>(/api/auth/login)
    participant AS as AuthService
    participant AM as AuthenticationManager
    participant UDS as CustomUserDetailsService
    participant UR as UserRepository
    participant JWT as JwtService

    Client->>AC: POST /api/auth/login {username, password}
    AC->>AS: login(request)
    AS->>AM: authenticate(username, password)
    AM->>UDS: loadUserByUsername(username)
    UDS->>UR: findByUsername(username)
    UR-->>UDS: User (hashed password)
    UDS-->>AM: UserDetails
    AM-->>AS: Authentication (verified via BCrypt)
    AS->>JWT: generateToken(userDetails)
    JWT-->>AS: signed JWT
    AS-->>AC: LoginResponse {token, type=Bearer}
    AC-->>Client: 200 OK {token}
```

## 2. Tenant-scoped protected request

```mermaid
sequenceDiagram
    actor Client
    participant F as JwtAuthenticationFilter
    participant JWT as JwtService
    participant UDS as CustomUserDetailsService
    participant Ctrl as Controller (e.g. RecipeController)
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    Client->>F: GET /api/recipes (Authorization Bearer token)
    alt token missing or invalid
        F->>F: clear SecurityContext, continue chain
        Note over F,Ctrl: request stays unauthenticated,<br/>authenticationEntryPoint returns 401
        F-->>Client: 401 Unauthorized
    else token valid
        F->>JWT: extractUsername(token)
        F->>UDS: loadUserByUsername(username)
        UDS-->>F: UserDetails
        F->>JWT: isTokenValid(token, userDetails)
        F->>F: set SecurityContext authentication, continue chain
        F->>Ctrl: forward request
        Ctrl->>Ctrl: read username from authenticated Principal
        Ctrl->>Svc: getRecipeBook(username)
        Svc->>Repo: findVisibleToUser(username)
        Repo->>DB: SELECT ... WHERE is_global = true<br/>OR owner.username = :username
        DB-->>Repo: global and tenant-owned rows
        Repo-->>Svc: entities
        Svc-->>Ctrl: List of RecipeResponseDto
        Ctrl-->>Client: 200 OK JSON
    end
```

## Notes

- The filter runs before `UsernamePasswordAuthenticationFilter`
  (`addFilterBefore` in `SecurityConfig`).
- On a missing/invalid token, the configured `authenticationEntryPoint` returns
  `401 Unauthorized`.
- Authentication identifies the caller; tenant authorization determines which records that caller may access.
- Controllers derive the tenant identity exclusively from the authenticated `Principal`. Request bodies and path
  parameters must not select an owner.
- Services pass that identity through the tenant seam, and repositories use owner-scoped or visibility-scoped queries.
  Unscoped calls such as `findAll()`, `findById()`, and `deleteById()` must not be used for tenant-owned data.
- Regular users may read their own and explicitly global records. Writes create private records owned by the
  authenticated user, and inaccessible foreign records are reported as `404 Not Found`.
- Passwords are stored as BCrypt hashes (`PasswordEncoder` bean); default token
  lifetime is configurable in `application.properties`.
