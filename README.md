a simple meal planner.

## Lokaler Start

1. Die Datei `.env` im Projektroot muss vorhanden sein.
2. PostgreSQL starten:
   `docker compose up -d`
3. Anwendung starten:
   `./mvnw spring-boot:run`
4. API testen:
   `http://localhost:8080/api/pantry`

Mealplans werden per `POST /api/mealplans/{date}` gespeichert oder aktualisiert.

Wenn im Browser JSON oder `[]` angezeigt wird, laeuft das Backend.

***WICHTIG:***

PRs werden nur noch gemerged, wenn die CI Pipeline erfolgreich durchgelaufen ist. 

Der korrekte Durchlauf kann vor jedem Push durch lokales Ausführen von 

**mvn spotless:apply**

und

**mvn clean verify**

überprüft werden. Wenn dabei Fehlermeldungen auftreten, müssen diese vor dem Push beseitigt werden.



functionalities to implement:

if one recipe is accepted, remove groceries depending on amount of food that is cooked

generate shopping list for weekly meal plan

groceries have an expiration date, mealplanning should be made accordingly to prevent spoilage

javafx gui 
 

test AB
