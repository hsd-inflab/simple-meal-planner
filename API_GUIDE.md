# REST-API Leitfaden

## Was neu ist

Das Projekt hat jetzt neben der JavaFX-Anwendung auch eine REST-API.

Das bedeutet:
- Die Fachlogik bleibt in den Services.
- Die neuen Controller stellen HTTP-Endpunkte bereit.
- Externe Clients wie Postman, ein Browser oder spaeter ein Web-Frontend koennen damit auf die Daten zugreifen.

Wichtig:
- JavaFX nutzt aktuell weiter direkte Service-Aufrufe.
- Die REST-API ist ein zusaetzlicher Zugang zum selben Backend.
- Die Browser-URL `http://localhost:8080/` hat keine eigene Hauptseite und zeigt deshalb keine Web-Oberflaeche.
- Die API liegt unter `http://localhost:8080/api/...`.

## Was die API aktuell kann

Es gibt drei Bereiche:
- Pantry: Vorrat ansehen, anlegen und loeschen
- Recipes: Rezepte ansehen, anlegen, verfuegbare Rezepte abfragen und loeschen
- Mealplans: Mealplaene ansehen, speichern und loeschen

### Pantry
- `GET /api/pantry`
  - liefert alle Pantry-Eintraege
- `GET /api/pantry/{id}`
  - liefert genau einen Pantry-Eintrag
- `POST /api/pantry`
  - legt einen neuen Pantry-Eintrag an
- `DELETE /api/pantry/{id}`
  - loescht einen Pantry-Eintrag

### Recipes
- `GET /api/recipes`
  - liefert alle Rezepte
- `GET /api/recipes/{id}`
  - liefert genau ein Rezept
- `GET /api/recipes/available`
  - liefert nur Rezepte, die mit dem aktuellen Vorrat machbar sind
- `POST /api/recipes`
  - legt ein neues Rezept an
- `DELETE /api/recipes/{id}`
  - loescht ein Rezept

### Mealplans
- `GET /api/mealplans`
  - liefert alle Mealplaene
- `GET /api/mealplans?start=2026-04-20&end=2026-04-26`
  - liefert Mealplaene in einem Zeitraum
- `GET /api/mealplans/{date}`
  - liefert den Mealplan fuer ein Datum
- `POST /api/mealplans/{date}`
  - speichert oder aktualisiert den Mealplan fuer dieses Datum
- `DELETE /api/mealplans/{date}`
  - loescht den Mealplan fuer dieses Datum

## Starten

1. PostgreSQL starten:
   ```powershell
   docker compose up -d
   ```
2. Spring Boot starten:
   ```powershell
   ./mvnw spring-boot:run
   ```
3. Test im Browser:
   ```text
   http://localhost:8080/api/pantry
   ```

Wenn dort JSON oder `[]` erscheint, laeuft das Backend.

## Browser vs. Postman

Im Browser kannst du nur einfache `GET`-Requests bequem testen.

Beispiele:
- `http://localhost:8080/api/pantry`
- `http://localhost:8080/api/recipes`
- `http://localhost:8080/api/mealplans`

Mit Postman testest du vor allem:
- `POST`
- `DELETE`
- komplexere `GET`-Requests

## Postman Schritt fuer Schritt

### 1. Alle Rezepte lesen
- Methode: `GET`
- URL:
  ```text
  http://localhost:8080/api/recipes
  ```

Erwartung:
- `200 OK`
- Response-Body ist JSON
- am Anfang oft `[]`

### 2. Pantry-Eintrag anlegen
- Methode: `POST`
- URL:
  ```text
  http://localhost:8080/api/pantry
  ```
- Header:
  - `Content-Type: application/json`
- Body, Typ `raw`, Format `JSON`:

```json
{
  "name": "Milch",
  "unit": "L",
  "amount": 1.0,
  "category": "DAIRY",
  "expirationDate": "2026-04-25",
  "purchaseDate": "2026-04-20",
  "brand": "Testmarke",
  "price": 1.49
}
```

Erwartung:
- `201 Created`
- im Response-Body steht das neue Objekt mit `id`
- im Header steht normalerweise `Location`

Danach pruefen mit:
- `GET http://localhost:8080/api/pantry`

### 3. Rezept anlegen
- Methode: `POST`
- URL:
  ```text
  http://localhost:8080/api/recipes
  ```
- Header:
  - `Content-Type: application/json`
- Body:

```json
{
  "name": "Muesli",
  "description": "Schnelles Fruehstueck",
  "ingredientsPerPerson": [
    {
      "name": "Milch",
      "unit": "L",
      "amount": 0.2,
      "category": "DAIRY",
      "foodType": "Milchprodukt",
      "preparation": ""
    }
  ]
}
```

Erwartung:
- `201 Created`
- Response-Body enthaelt das gespeicherte Rezept mit `id`

Danach pruefen mit:
- `GET http://localhost:8080/api/recipes`

### 4. Verfuegbare Rezepte pruefen
- Methode: `GET`
- URL:
  ```text
  http://localhost:8080/api/recipes/available
  ```

Erwartung:
- Das Rezept erscheint dort nur, wenn genug Zutaten im Pantry vorhanden sind.

### 5. Mealplan speichern
Dafuer brauchst du eine existierende Rezept-ID.
Die bekommst du z. B. aus:
- `GET /api/recipes`

Dann:
- Methode: `POST`
- URL:
  ```text
  http://localhost:8080/api/mealplans/2026-04-21
  ```
- Header:
  - `Content-Type: application/json`
- Body:

```json
{
  "breakfastRecipe": {
    "id": "REZEPT-ID-HIER"
  },
  "lunchRecipe": null,
  "dinnerRecipe": null,
  "breakfastServings": 2,
  "lunchServings": 0,
  "dinnerServings": 0
}
```

Erwartung:
- `200 OK`
- Response-Body zeigt den gespeicherten Mealplan

Danach pruefen mit:
- `GET http://localhost:8080/api/mealplans/2026-04-21`

### 6. Daten loeschen
#### Pantry-Eintrag loeschen
- Methode: `DELETE`
- URL:
  ```text
  http://localhost:8080/api/pantry/PANTRY-ID-HIER
  ```

Erwartung:
- `204 No Content`

#### Rezept loeschen
- Methode: `DELETE`
- URL:
  ```text
  http://localhost:8080/api/recipes/REZEPT-ID-HIER
  ```

Erwartung:
- `204 No Content`

#### Mealplan loeschen
- Methode: `DELETE`
- URL:
  ```text
  http://localhost:8080/api/mealplans/2026-04-21
  ```

Erwartung:
- `204 No Content`

## Typische HTTP-Statuscodes
- `200 OK`: Request war erfolgreich
- `201 Created`: Objekt wurde neu angelegt
- `204 No Content`: Objekt wurde geloescht, ohne Body in der Antwort
- `404 Not Found`: Ressource wurde nicht gefunden

## Was du in der JavaFX-App siehst

Die JavaFX-Anwendung benutzt aktuell nicht die REST-API, sondern direkt die Services im Backend.

Deshalb gilt:
- Wenn du in JavaFX ein Rezept anlegst, siehst du keinen HTTP-Header.
- Wenn du mit Postman arbeitest, siehst du HTTP-Status, Header und JSON-Responses.

## Woran du erkennst, dass alles funktioniert
- `http://localhost:8080/api/pantry` liefert JSON
- `POST` in Postman liefert `201 Created` oder `200 OK`
- `DELETE` liefert `204 No Content`
- neu angelegte Daten erscheinen danach bei `GET`
- die Anwendung startet mit Datenbankverbindung ohne Fehler
