# Lokaler Test: Branch `feature-63-base-security`

Ziel: Backend lokal starten, Default-User automatisch in PostgreSQL anlegen lassen, Login testen und mit Bearer Token auf eine geschuetzte API zugreifen.

## Voraussetzungen

Installiert sein sollte:

```text
Java 21
Docker Desktop
Git
PowerShell oder Terminal
```

## 1. Branch holen

```powershell
git fetch
git checkout feature-63-base-security
git pull
```

## 2. Environment setzen

Das Projekt nutzt PostgreSQL ueber `compose.yaml`. Dafuer muessen diese Variablen gesetzt sein:

```powershell
$env:LOCAL_DB_NAME="mealplanner"
$env:LOCAL_DB_USER="mealplanner"
$env:LOCAL_DB_PASSWORD="mealplanner"
$env:RECIPE_API_KEY="test"
$env:RECIPE_API_PWHASH="test"
```

Hinweis: Diese Werte gelten nur fuer das aktuelle PowerShell-Fenster.

## 3. PostgreSQL starten

```powershell
docker compose up -d
```

PostgreSQL laeuft danach lokal auf:

```text
localhost:5432
```

## 4. Spring Boot Backend starten

```powershell
.\mvnw spring-boot:run
```

Beim Start passiert automatisch:

```text
Spring Boot verbindet sich mit PostgreSQL
Hibernate legt fehlende Tabellen an
DataLoader prueft, ob der Default-User existiert
Wenn nicht: User admin wird mit BCrypt-Passwort-Hash gespeichert
```

Default-Login aus `application.properties`:

```text
username: admin
password: secret
```

## 5. Login testen

In einem zweiten PowerShell-Fenster:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/auth/login" `
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

```powershell
Invoke-WebRequest `
  -Uri "http://localhost:8080/api/pantry"
```

Erwartung:

```text
401 Unauthorized
```

Das ist korrekt, weil `/api/**` geschuetzt ist.

## 7. Geschuetzte API mit Token testen

Token aus Schritt 5 in eine Variable legen:

```powershell
$response = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"secret"}'

$token = $response.token
```

Dann Request mit Authorization-Header:

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/pantry" `
  -Headers @{ Authorization = "Bearer $token" }
```

Erwartung:

```text
200 OK
```

Je nach Datenstand kommt eine Liste oder eine leere Antwort zurueck.

## Wichtig fuers Meeting

Aktuell gibt es noch keine Web-Login-Seite. Man loggt sich momentan nur ueber die REST-API ein. Der Bearer Token wird in der Login-Response zurueckgegeben und muss vom spaeteren Webfrontend oder JavaFX-Client bei geschuetzten API-Requests im Header mitgeschickt werden:

```http
Authorization: Bearer <token>
```

## Next Steps

### 1. Login-Request validieren

Aktuell existieren `LoginRequest` und `AuthController`, aber fehlende oder leere Felder sind noch nicht sauber als `400 Bad Request` abgesichert.

Ziel:

```text
POST /api/auth/login mit leerem username -> 400
POST /api/auth/login mit leerem password -> 400
POST /api/auth/login mit falschen Credentials -> 401
```

Technisch:

```text
spring-boot-starter-validation ergaenzen
LoginRequest mit @NotBlank annotieren
AuthController mit @Valid erweitern
Tests fuer 400-Faelle schreiben
```

### 2. User-Anlage sauber entscheiden

Der aktuelle `DataLoader` ist fuer lokale Entwicklung ausreichend, aber keine endgueltige Benutzerverwaltung.

Optionen:

```text
Kurzfristig: DataLoader fuer Dev behalten
Mittelfristig: UserService fuer kontrollierte User-Anlage
Optional: POST /api/auth/register fuer Registrierung
Produktionsnah: Initialer Admin ueber Migration oder Admin-Prozess
```

Empfehlung:

```text
DataLoader nur fuer dev/test nutzen
Default-Passwort nicht dauerhaft in application.properties pflegen
Passwoerter immer mit BCrypt speichern
Keine Klartext-Passwoerter in DB, Logs oder Git
```

### 3. Webfrontend oder JavaFX-Client an Auth anbinden

Aktuell kann man sich nur ueber API-Clients einloggen. Fuer echte Nutzung braucht ein Client einen Login-Flow.

Ziel fuer ein Webfrontend:

```text
Login-Seite anzeigen
POST /api/auth/login senden
Token aus Response lesen
Token clientseitig speichern
Authorization: Bearer <token> bei API-Requests mitsenden
Bei 401 zur Login-Seite zurueckfuehren
```

Ziel fuer JavaFX:

```text
Login-Dialog bauen
HTTP-Client gegen /api/auth/login verwenden
Token im App-State halten
Token bei REST-Requests mitsenden
Logout entfernt Token aus App-State
```

### 4. Token-Handling haerten

Aktuell gibt es Access Tokens mit Ablaufzeit. Fuer den naechsten Sicherheitsausbau sollten klare Regeln definiert werden.

Zu klaeren:

```text
Wie lange soll ein Token gueltig sein?
Brauchen wir Refresh Tokens?
Wo speichert ein Webfrontend den Token?
Wie funktioniert Logout?
Welche Rollen brauchen wir: USER, ADMIN?
```

### 5. Security-Tests erweitern

Vor einem Merge sollten folgende Faelle dauerhaft getestet sein:

```text
Login mit gueltigen Credentials -> 200 + token
Login mit falschem Passwort -> 401
Login mit unbekanntem User -> 401
Login mit fehlendem username/password -> 400
GET /api/** ohne Token -> 401
GET /api/** mit ungueltigem Token -> 401
GET /api/** mit gueltigem Token -> 200
```

### 6. Konfiguration aufraeumen

Vor Merge oder Demo sollte geprueft werden:

```text
jwt.secret nicht produktiv im Git verwenden
Dev-User klar als Dev-Seed markieren
application-test.properties fuer Tests stabil halten
Docker Compose Start in Tests deaktiviert lassen
README oder SECURITY_LOCAL_TEST_GUIDE aktuell halten
```

## Falls Login nicht klappt

Wenn `admin/secret` nicht funktioniert, existiert wahrscheinlich schon ein alter `admin` in der DB mit anderem Passwort.

Dann entweder DB-Container/Volume zuruecksetzen oder in `application.properties` testweise einen neuen Usernamen setzen:

```properties
app.security.user.name=marvin
app.security.user.password=test123
```

Danach Backend neu starten und mit `marvin/test123` einloggen.
