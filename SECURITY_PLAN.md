# REST Security Plan

## Ziel

Die REST-API soll nur fuer authentifizierte Benutzer zugreifbar sein. Die Authentifizierung erfolgt ueber einen Login-Endpunkt. Nach erfolgreichem Login liefert der Server ein Bearer-Token zurueck, das der Client bei weiteren Requests im `Authorization`-Header mitsendet.

## Empfohlener Ansatz

Verwendet wird `Spring Security` in Kombination mit einem stateless Bearer-Token-Ansatz auf Basis von JWT.

Warum dieser Ansatz:

- Standardloesung im Spring-Boot-Umfeld
- Gut fuer REST-APIs geeignet
- Keine serverseitige Session-Verwaltung notwendig
- Rollen und Berechtigungen spaeter leicht erweiterbar

## Architektur

### 1. Abhaengigkeiten erweitern

In `pom.xml` werden benoetigt:

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- JWT-Bibliothek, z. B. `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

Optional spaeter:

- `spring-boot-starter-validation`

### 2. Benutzerquelle festlegen

Zunaechst muss festgelegt werden, wo erlaubte Benutzer liegen:

- Einfacher Start: In-Memory-User fuer Entwicklung
- Solider naechster Schritt: Benutzer in der Datenbank speichern

Empfehlung fuer dieses Projekt:

- Erst mit einem einzelnen konfigurierten Benutzer starten
- Danach auf persistente Benutzerverwaltung erweitern

### 3. Passwort-Handling korrigieren

Der bestehende `PasswordService` nutzt SHA-256. Fuer Passwoerter sollte stattdessen `BCryptPasswordEncoder` aus Spring Security verwendet werden.

Ziel:

- Keine eigene Passwort-Hash-Logik mehr
- Passwoerter nur als BCrypt-Hash speichern

### 4. Authentifizierungs-Endpunkt

Neuer Endpunkt:

- `POST /api/auth/login`

Request-Beispiel:

```json
{
  "username": "admin",
  "password": "secret"
}
```


Verhalten:

1. Benutzername und Passwort entgegennehmen
2. Benutzer laden
3. Passwort mit Spring Security pruefen
4. Bei Erfolg JWT erzeugen
5. Token an Client zurueckgeben

Response-Empfehlung:

```json
{
  "token": "<jwt-token>",
  "type": "Bearer"
}
```

Hinweis:

Das Token sollte vorzugsweise im Response-Body und nicht nur im Header geliefert werden. Das ist fuer REST-Clients ueblicher und leichter nutzbar.

### 5. JWT-Service

Ein `JwtService` uebernimmt:

- Token erzeugen
- Ablaufzeit setzen
- Claims eintragen, z. B. Benutzername und Rollen
- Token validieren
- Benutzerinformationen aus dem Token lesen

Typische Inhalte eines Tokens:

- Subject: Benutzername
- Issued-At
- Expiration
- Rollen oder Authorities

### 6. Security-Konfiguration

Eine `SecurityConfig` definiert:

- Welche Endpunkte offen sind
- Welche Endpunkte Authentifizierung brauchen
- Stateless Security
- Passwort-Encoder
- Authentication Provider
- Filter-Reihenfolge

Typische Regeln:

- `/api/auth/**` ist frei
- `/api/**` ist geschuetzt
- CSRF fuer reine REST-API deaktivieren
- Session-Policy auf `STATELESS`

### 7. Bearer-Token-Filter

Ein eigener Request-Filter liest:

- `Authorization: Bearer <token>`

Aufgaben des Filters:

1. Header auslesen
2. Token extrahieren
3. Token validieren
4. Benutzer laden
5. `SecurityContext` setzen

Damit koennen geschuetzte Controller automatisch ueber Spring Security abgesichert werden.

### 8. Geschuetzte REST-Endpunkte

Bestehende oder neue REST-Controller unter `/api/**` werden abgesichert.

Optional spaeter:

- Rollenpruefung mit `hasRole(...)`
- Method Security mit `@PreAuthorize`

## Technische Umsetzungsreihenfolge

1. `spring-boot-starter-web` und `spring-boot-starter-security` in `pom.xml` aufnehmen
2. JWT-Abhaengigkeiten ergaenzen
3. `SecurityConfig` anlegen
4. `BCryptPasswordEncoder` einfuehren
5. Login-Request- und Response-DTOs anlegen
6. `AuthController` mit `POST /api/auth/login` bauen
7. `JwtService` implementieren
8. `JwtAuthenticationFilter` implementieren
9. Erste geschuetzte Test-Route anlegen
10. Bestehende REST-Endpunkte schrittweise unter Security stellen
11. Tests fuer Login und Zugriffsschutz schreiben

## Benoetigte Klassen

Voraussichtlich:

- `config/SecurityConfig.java`
- `security/JwtService.java`
- `security/JwtAuthenticationFilter.java`
- `controller/AuthController.java`
- `dto/LoginRequest.java`
- `dto/LoginResponse.java`

Je nach Ausbaustufe zusaetzlich:

- `entity/User.java`
- `repository/UserRepository.java`
- `service/CustomUserDetailsService.java`

## Tests

Es sollten mindestens folgende Faelle getestet werden:

- Login mit gueltigen Credentials liefert Token
- Login mit ungueltigen Credentials liefert `401`
- Zugriff auf geschuetzten Endpunkt ohne Token liefert `401`
- Zugriff mit ungueltigem Token liefert `401`
- Zugriff mit gueltigem Token funktioniert

## Offene Entscheidungen

Vor der Umsetzung sollten diese Punkte festgelegt werden:

- Soll es zunaechst nur einen festen Benutzer geben oder echte Benutzer in der Datenbank?
- Soll das Token nur fuer die Laufzeit der App gelten oder eine feste Ablaufzeit haben?
- Werden Rollen wie `ADMIN` und `USER` benoetigt?
- Soll spaeter ein Refresh-Token-Mechanismus kommen?

## Empfehlung fuer den ersten Implementierungsschritt

Fuer einen sauberen Start:

1. Spring Web und Spring Security aktivieren
2. Einen einfachen Login-Endpunkt bauen
3. Einen einzelnen konfigurierten Benutzer verwenden
4. JWT-Bearer-Authentifizierung einfuehren
5. Danach Benutzer persistent in PostgreSQL ablegen

## Ergebnis nach Phase 1

Nach der ersten Ausbaustufe soll folgendes funktionieren:

- `POST /api/auth/login` authentifiziert Benutzer
- Der Server liefert ein Bearer-Token zurueck
- Alle geschuetzten `/api/**`-Endpunkte akzeptieren nur gueltige Tokens
- Unautorisierte Zugriffe liefern `401 Unauthorized`
