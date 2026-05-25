# ADR 0002: User- und Rollenmodell fuer Authentifizierung

- Status: Accepted
- Datum: 2026-05-25

## Kontext

Im Auth-Pfad gab es mehrere Modellierungsprobleme:

- `User.id` wurde nicht durch JPA/Hibernate generiert
- das Passwortfeld hiess `password`, obwohl ein Hash gespeichert wird
- Rollen wurden als kommagetrennter String gespeichert
- das Rollen-Mapping im `CustomUserDetailsService` war anfaellig fuer `NullPointerException`

Diese Punkte erschweren Wartung, machen das Modell fachlich unklar und fuehren leicht zu Laufzeitfehlern.

## Entscheidung

Das User-Modell wird auf ein expliziteres und robusteres Persistenzmodell umgestellt.

Technische Entscheidungen:

- `User.id` wird mit `@GeneratedValue(strategy = GenerationType.UUID)` durch Hibernate erzeugt
- das Java-Feld fuer das gespeicherte Passwort heisst `passwordHash`
- das Feld bleibt aus Kompatibilitaetsgruenden auf die bestehende Datenbankspalte `password` gemappt
- Rollen werden als `List<Role>` mit `Role`-Enum modelliert
- Rollen werden per `@ElementCollection` in einer eigenen Tabelle `app_user_roles` gespeichert
- der `CustomUserDetailsService` mappt `Role` direkt auf Spring Security Authorities

## Ablauf

### User anlegen

1. Ein neuer `User` wird ohne manuelle UUID erzeugt.
2. Hibernate vergibt die UUID beim Persistieren.
3. Das Passwort wird vor dem Speichern gehasht.
4. Die Rollenliste wird in `app_user_roles` persistiert.

### Login / UserDetails laden

1. `CustomUserDetailsService` laedt den User ueber `findByUsername(...)`.
2. Die Rollen werden als `List<Role>` geladen.
3. Jede Rolle wird auf `ROLE_<ENUM_NAME>` gemappt.
4. Spring Security erhaelt den User mit Username, `passwordHash` und Authorities.

## Konsequenzen

Positiv:

- klareres Domänenmodell
- kein String-Parsen fuer Rollen mehr
- kein `split(",")` und damit kein entsprechender NPE-Pfad mehr
- konsistentes UUID-Handling wie bei anderen Entities
- klarere Benennung fuer Passwort-Hashes

Negativ:

- die Rollenpersistenz ist eine echte Schemaaenderung
- bestehende Datenbanken brauchen eine Migration von der alten Rollenstruktur auf `app_user_roles`
- das Auth-Modell ist jetzt enger an JPA-Collection-Mapping gebunden
