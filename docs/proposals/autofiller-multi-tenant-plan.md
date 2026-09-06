# Plan: DatabaseAutofillerService – Multi-Tenant & Prod-Schutz

**Status:** Entwurf / zur Diskussion im Team
**Datum:** 2026-07-20

## Worum geht es?

Der `DatabaseAutofillerService` befüllt eine leere Datenbank mit Demodaten für die Entwicklung.
Durch die Umstellung auf Multi-Tenant muss er angepasst werden:

1. Die Demodaten sollen einem festen **TestUser** gehören (nicht mehr "global").
2. Der Autofiller darf **niemals in Produktion** laufen.
3. Zukünftig gibt es **zwei Datenbank-Container**: einer für Test, einer für Prod.

## Ist-Zustand

- Der Autofiller ist ein globaler `@Component` + `CommandLineRunner` → läuft **immer, in jedem Profil**. Es gibt keinen Umgebungs-Guard.
- Er prüft per SQL, ob die vier Fachtabellen (`daily_meal`, `pantry`, `recipe_book`, `recipe_ingredients`) **global** leer sind.
- Die Entities (`PantryItem`, `Recipe`, `DailyMeal`) haben **noch keine User-Referenz** – die Multi-Tenant-Umstellung der Entities ist Voraussetzung für diesen Plan.
- Gutes Vorbild existiert schon: der `DataLoader` legt den Seed-User nur an, wenn `app.security.seed-default-user.enabled=true` gesetzt ist (**opt-in, standardmäßig aus**).
- Profile: `local` (Default) und `docker`. Ein `prod`-Profil gibt es noch nicht.

## Geplante Lösung (Kurzfassung)

### 1. Prod-Schutz: doppelt absichern

| Maßnahme | Wirkung |
|---|---|
| `app.autofill.enabled=false` als Default + `@ConditionalOnProperty` | Autofiller ist überall **aus**, außer man schaltet ihn explizit ein (fail-closed, gleiches Muster wie beim Seed-User) |
| Zusätzlich `@Profile("!prod")` | Selbst wenn jemand das Flag versehentlich in Prod setzt, wird die Bean nicht erzeugt |

Wichtig: `@Profile("!prod")` **allein** reicht nicht – bei einem neuen/unbekannten Profil würde der Autofiller sonst laufen ("fail open"). Deshalb die Kombination.

### 2. Multi-Tenant: Daten gehören dem TestUser

- Neuer Config-Wert `app.autofill.username` (z. B. `testuser`).
- Beim Start: TestUser auflösen bzw. anlegen, dann alle Demodaten (Pantry, Rezepte, DailyMeals) mit diesem User als Owner speichern.
- Die Leer-Prüfung ändert sich von *"sind die Tabellen leer?"* zu *"**hat der TestUser schon Daten?**"* (Repository-Abfrage statt rohem SQL).
  → Daten echter Nutzer verhindern das Seeding nicht und werden davon nie berührt.

### 3. Zwei DB-Container (Test / Prod)

Empfehlung: **Compose-Basis + Override-Datei**

- `compose.yaml` (wie bisher, Dev/Test): Test-Postgres mit eigenem Volume, Autofill **an**, `SPRING_PROFILES_ACTIVE=docker`.
- `compose.prod.yaml` (neu): Prod-Postgres mit **eigenem Volume, eigenen Credentials (`PROD_DB_*`), ohne Host-Port-Mapping**, `SPRING_PROFILES_ACTIVE=docker,prod`, Autofill-Flag gar nicht gesetzt.
- Die Datenbank-Verbindung bleibt wie heute über Umgebungsvariablen gesteuert; das `prod`-Profil steuert nur das Verhalten (Autofill aus etc.).

## Offene Fragen (bitte im Team klären)

### Frage 1: Wer legt den TestUser an?

| Option | Beschreibung | Bewertung |
|---|---|---|
| **A: Zusammenführen zu einem `TestDataSeeder`** (Empfehlung) | Seed-User (`DataLoader`) und Autofiller werden eine Komponente: legt erst den TestUser an, dann dessen Demodaten | Abhängigkeit ist explizit, keine versteckte Reihenfolge, ein Schalter für alles |
| B: Getrennt lassen + `@Order` | `DataLoader` läuft zuerst, Autofiller danach; Autofiller setzt voraus, dass der User existiert | Weniger Umbau, aber implizite Reihenfolgen-Abhängigkeit zwischen zwei Runnern |

### Frage 2: Zwei Container – eine oder zwei Compose-Dateien?

| Option | Beschreibung | Bewertung |
|---|---|---|
| **A: Basis + Override (`compose.yaml` + `compose.prod.yaml`)** (Empfehlung) | Test und Prod getrennt startbar, Prod-Datei bleibt restriktiv und separat deploybar | Klare Trennung, kein versehentliches Mitstarten |
| B: Eine Datei mit Docker-Compose-Profiles (`--profile test` / `--profile prod`) | Beide DB-Container in einer Datei, Auswahl beim Start | Praktisch, wenn beide gleichzeitig auf demselben Host laufen sollen |

→ Hängt davon ab: **Sollen Test- und Prod-Container gleichzeitig auf derselben Maschine laufen?**

### Frage 3: Was passiert, wenn der TestUser fehlt (bei Option 1B)?

- Fehler werfen und Start abbrechen? Oder nur Warnung loggen und Seeding überspringen?
- Bei Option 1A stellt sich die Frage nicht – der Seeder legt den User selbst an.

### Frage 4: Name und Zugangsdaten des TestUsers

- Festen Namen (`testuser`) vorgeben oder komplett über Config/Env steuern?
- Passwort darf nicht im Repo landen → wie bisher über Env-Variable.

## Umsetzungsreihenfolge

1. `app.autofill.enabled`-Flag + `@ConditionalOnProperty` (fail-closed Default)
2. `prod`-Profil einführen + `@Profile("!prod")` ergänzen
3. Autofiller auf TestUser umstellen (Owner setzen, per-User-Leerprüfung)
4. Compose-Split: Test-/Prod-Container mit getrennten Volumes und Credentials

**Voraussetzung:** Die Entities brauchen zuerst eine User-Referenz (FK + Flyway-Migration) – das ist ein eigener Task.

## Erfolgskriterien

- Autofiller läuft nur mit explizit gesetztem Flag und nie im `prod`-Profil.
- Alle Demodaten gehören dem TestUser; Daten anderer User beeinflussen das Seeding nicht.
- Test- und Prod-DB sind über getrennte Container, Volumes und Credentials isoliert.

