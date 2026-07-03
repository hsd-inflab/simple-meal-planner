# Lokaler Security- und API-Test

Ziel: PostgreSQL lokal starten, Flyway-Migrationen ausfuehren, Spring Boot lokal starten, einen lokalen Test-User seeden, Login testen und mit Bearer Token auf eine geschuetzte API zugreifen.

## Voraussetzungen

Installiert sein sollte:

```text
Java 21
Docker Desktop
Git
PowerShell oder Terminal
```

## 1. Aktuellen Stand holen

```powershell
git fetch
git pull
```

Optional pruefen, auf welchem Branch du bist:

```powershell
git branch --show-current
```

## 2. `.env` pruefen

Das Projekt nutzt PostgreSQL ueber `compose.yaml` und laedt beim lokalen Spring-Boot-Start die Datei `.env` aus dem Projektroot.

Du musst die Werte auf macOS/zsh normalerweise nicht jedes Mal ins Terminal schreiben. Wichtig ist nur, dass `.env` die benoetigten Keys enthaelt.

Fuer den lokalen Login-Test sollten diese Werte in `.env` stehen:

```properties
LOCAL_DB_NAME=mealplanner
LOCAL_DB_USER=mealplanner
LOCAL_DB_PASSWORD=mealplanner
RECIPE_API_KEY=test
RECIPE_API_PWHASH=test
app.security.seed-default-user.enabled=true
app.security.user.name=admin
app.security.user.password=secret
```

Hinweis fuer macOS/zsh:

```text
Nicht `source .env` ausfuehren.
Die Datei enthaelt auch Property-Namen mit Punkten, die fuer zsh keine normalen Shell-Variablen sind.
Spring Boot liest die Datei selbst beim Start.
```

Alternative ohne `.env`: Werte nur fuer einen einzelnen lokalen Maven-Start setzen.

```bash
export LOCAL_DB_NAME="mealplanner"
export LOCAL_DB_USER="mealplanner"
export LOCAL_DB_PASSWORD="mealplanner"
export RECIPE_API_KEY="test"
export RECIPE_API_PWHASH="test"
export APP_SECURITY_SEED_DEFAULT_USER_ENABLED="true"
export APP_SECURITY_USER_NAME="admin"
export APP_SECURITY_USER_PASSWORD="secret"
./mvnw spring-boot:run
```

Diese `export`-Werte gelten nur fuer das aktuelle Terminal-Fenster.

## 3. PostgreSQL und Flyway starten

Fuer den lokalen Maven-Start sollen nur PostgreSQL und Flyway aus Docker Compose laufen, nicht der Backend-Container:

```bash
docker compose up -d postgres flyway
```

Danach gilt:

```text
PostgreSQL: localhost:5432
Migrationen: durch Flyway ausgefuehrt
```

Warum nicht einfach `docker compose up -d`?

```text
docker compose up -d startet auch den Backend-Container auf Port 8080.
Der lokale Spring-Boot-Start nutzt im local-Profil Port 8081.
```

## 4. Spring Boot Backend lokal starten

Bash/zsh:

```bash
./mvnw spring-boot:run
```

PowerShell:

```powershell
.\mvnw spring-boot:run
```

Beim Start passiert:

```text
Spring Boot verbindet sich mit PostgreSQL auf localhost:5432
Hibernate validiert das Schema gegen die Flyway-Migrationen
DataLoader legt den Seed-User nur an, wenn `app.security.seed-default-user.enabled=true` gesetzt ist
Das lokale Backend laeuft auf Port 8081
```

Default-Login fuer diesen lokalen Test:

```text
username: admin
password: secret
```

## 5. Login testen

Bash/zsh auf macOS:

```bash
curl -i -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}'
```

PowerShell auf Windows:


```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"secret"}'
```

Erwartung:

```json
{
  "token": "eyJ...",
  "type": "Bearer"
}
```

Der `token` ist das JWT. `Bearer` bedeutet: Der Client darf diesen Token bei weiteren Requests als Zugriffsnachweis mitsenden.

## 6. Geschuetzte API ohne Token testen

Bash/zsh auf macOS:

```bash
curl -i "http://localhost:8081/api/pantry"
```

PowerShell auf Windows:

```powershell
Invoke-WebRequest `
  -Uri "http://localhost:8081/api/pantry"
```

Erwartung:

```text
401 Unauthorized
```

Das ist korrekt, weil `/api/**` geschuetzt ist. Ausgenommen ist nur `/api/auth/**`.

## 7. Geschuetzte API mit Token testen

Token aus Schritt 5 in eine Variable legen.

Bash/zsh auf macOS:

```bash
TOKEN=$(curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

PowerShell auf Windows:

```powershell
$response = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"secret"}'

$token = $response.token
```

Dann Request mit Authorization-Header.

Bash/zsh auf macOS:

```bash
curl -i "http://localhost:8081/api/pantry" \
  -H "Authorization: Bearer $TOKEN"
```

PowerShell auf Windows:

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8081/api/pantry" `
  -Headers @{ Authorization = "Bearer $token" }
```

Erwartung:

```text
200 OK
```

Je nach Datenstand kommt eine Liste oder eine leere Antwort zurueck.

## 8. Ungueltiges Token testen

Bash/zsh auf macOS:

```bash
curl -i "http://localhost:8081/api/pantry" \
  -H "Authorization: Bearer invalid-token"
```

PowerShell auf Windows:

```powershell
Invoke-WebRequest `
  -Uri "http://localhost:8081/api/pantry" `
  -Headers @{ Authorization = "Bearer invalid-token" }
```

Erwartung:

```text
401 Unauthorized
```

## Optional: Backend komplett ueber Docker Compose starten

Wenn du nicht lokal mit Maven starten willst, kannst du den Backend-Container nutzen:

```powershell
.\mvnw clean package -DskipTests
docker compose up -d
```

Bash/zsh:

```bash
./mvnw clean package -DskipTests
docker compose up -d
```

Dann laufen die API-Requests gegen Port `8080` statt `8081`:

```text
http://localhost:8080/api/auth/login
http://localhost:8080/api/pantry
```

Wichtig: Der Backend-Container liest `.env` ueber `env_file`. Wenn du Docker Compose komplett nutzt, sollten die Security-Werte fuer den Container als Environment-Variablen in `.env` stehen:

```properties
LOCAL_DB_NAME=mealplanner
LOCAL_DB_USER=mealplanner
LOCAL_DB_PASSWORD=mealplanner
RECIPE_API_KEY=test
RECIPE_API_PWHASH=test
APP_SECURITY_SEED_DEFAULT_USER_ENABLED=true
APP_SECURITY_USER_NAME=admin
APP_SECURITY_USER_PASSWORD=secret
```

Fuer den lokalen Maven-Start koennen stattdessen die punktierten Properties aus Schritt 2 verwendet werden:

```properties
app.security.seed-default-user.enabled=true
app.security.user.name=admin
app.security.user.password=secret
```

## Aktueller Security-Stand

```text
POST /api/auth/login mit gueltigen Credentials -> 200 + token
POST /api/auth/login mit falschen Credentials -> 401
GET /api/** ohne Token -> 401
GET /api/** mit ungueltigem Token -> 401
GET /api/** mit gueltigem Token -> 200
```

Noch offen:

```text
LoginRequest hat aktuell noch keine @NotBlank-Validierung.
POST /api/auth/login mit leerem username/password ist deshalb noch nicht sauber als 400 Bad Request abgesichert.
```

## Falls Login nicht klappt

Wenn `admin/secret` nicht funktioniert, pruefe zuerst, ob der Seed-User wirklich aktiviert wurde:

```text
app.security.seed-default-user.enabled=true
app.security.user.name=admin
app.security.user.password=secret
```

Wenn du diese Werte gerade erst in `.env` eingetragen oder geaendert hast:

```text
Spring Boot stoppen
Spring Boot neu starten
Login erneut testen
```

Die `.env` wird beim Start der Anwendung geladen. Eine bereits laufende Spring-Boot-Instanz uebernimmt spaetere `.env`-Aenderungen nicht automatisch.

Optional kannst du pruefen, ob ueberhaupt ein User in PostgreSQL existiert:

```bash
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select username from app_user order by username;"'
```

Erwartung fuer den Default-Test:

```text
admin
```

Wenn kein User ausgegeben wird, wurde der Seed-User noch nicht angelegt. Starte dann Spring Boot neu und pruefe beim Start, ob keine Konfigurationsfehler auftreten.

Wenn in der Datenbank schon ein alter `admin` mit anderem Passwort existiert, legt der `DataLoader` keinen neuen User an. Dann entweder Datenbank/Volume zuruecksetzen oder fuer den lokalen Test einen anderen Usernamen setzen:

```powershell
$env:APP_SECURITY_USER_NAME="marvin"
$env:APP_SECURITY_USER_PASSWORD="test123"
```

Bash/zsh:

```bash
export APP_SECURITY_USER_NAME="marvin"
export APP_SECURITY_USER_PASSWORD="test123"
```

Danach Backend neu starten und mit dem neuen User einloggen.

## Tests ausfuehren

Der relevante Integrationstest fuer Login und geschuetzte API:

```powershell
.\mvnw -Dtest=AuthIntegrationTest test
```

Bash/zsh:

```bash
./mvnw -Dtest=AuthIntegrationTest test
```
