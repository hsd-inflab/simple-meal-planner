# ADR 0001: Default User Seeding nur per explizitem Opt-in

- Status: Accepted
- Datum: 2026-05-25

## Kontext

Die Anwendung hatte einen `DataLoader`, der in jeder Umgebung automatisch einen Default-User aus Properties angelegt hat. Dabei gab es implizite Fallback-Credentials (`admin` / `secret`).

Das ist für produktive Deployments riskant:

- bekannte Zugangsdaten koennen unbeabsichtigt in produktiven Umgebungen entstehen
- das Verhalten war standardmaessig aktiv statt bewusst eingeschaltet
- der Seeder lief unabhaengig vom Deploy-Kontext

## Entscheidung

Der Default-User-Seed wird nur noch bei explizitem Opt-in aktiviert.

Technische Umsetzung:

- `DataLoader` ist mit `@ConditionalOnProperty(...)` gegatet
- Aktivierung nur bei `app.security.seed-default-user.enabled=true`
- keine impliziten Fallback-Credentials mehr im Code
- Testprofil setzt die Property bewusst, damit Auth-Integrationstests weiter funktionieren

## Ablauf

1. Beim Start prueft Spring, ob `app.security.seed-default-user.enabled=true` gesetzt ist.
2. Nur dann wird der `DataLoader` als Bean aktiviert.
3. Der Seeder sucht nach dem konfigurierten Username.
4. Falls der User noch nicht existiert, wird er mit gehashtem Passwort angelegt.

## Konsequenzen

Positiv:

- kein unbeabsichtigter Seed-User in Produktion
- expliziter und nachvollziehbarer Aktivierungsmechanismus
- Tests koennen den Seed weiter bewusst nutzen

Negativ:

- lokale Entwicklerumgebungen muessen den Seed jetzt aktiv einschalten
- Tests, die implizit auf einen Seed-User verlassen, muessen das Testprofil korrekt setzen
