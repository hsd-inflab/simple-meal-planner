package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.dto.RecipeIngredientDto;
import hsd.inflab.smp.enums.SearchMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Dienst zum Abrufen von Rezepten aus dem Web
 */
@Service
public class RecipeAPIService {

    private final RecipeAPIParser recipeAPIParser;

    private final ConfigService configService;

    public RecipeAPIService(RecipeAPIParser recipeAPIParser, ConfigService configService) {
        this.recipeAPIParser = recipeAPIParser;
        this.configService = configService;
    }

    /**
     * Sucht Rezepttitel basierend auf einer Liste von Suchbegriffen.
     * Verwendet standardmäßig den Suchmodus `SearchMode.ANY`.
     *
     * @param terms Die Liste der Suchbegriffe.
     * @return Eine Liste von Rezepttiteln, die den Suchkriterien entsprechen.
     * @throws IOException Wenn ein Fehler bei der Kommunikation mit der API auftritt.
     * @throws InterruptedException Wenn der Vorgang unterbrochen wird.
     */
    public List<String> searchRecipeTitles(List<String> terms) throws IOException, InterruptedException {
        return searchRecipeTitles(
                terms,
                SearchMode.ANY); // ANY: Zeigt INGREDIANTEN und TITLE an, damit die Suche möglichst viele Ergebnisse
        // liefert, die dann in der UI gefiltert werden können.
    }

    public List<String> searchRecipeTitles(List<String> terms, SearchMode mode)
            throws IOException, InterruptedException {
        List<String> cleanedTerms = new ArrayList<>();

        if (terms != null) { // null-Werte werden abgefangen
            for (String term : terms) {
                String trimmed = term == null ? "" : term.trim(); // Leerzeichen werden entfernt (trim())
                if (!trimmed.isEmpty()) { // Leere Strings werden ignoriert
                    cleanedTerms.add(trimmed);
                }
            }
        }

        // Wenn keine Liste übergeben wurde oder alle Begriffe leer sind, geben wir eine leere Ergebnisliste zurück.
        // → kein API-Aufruf
        if (cleanedTerms.isEmpty()) {
            return List.of();
        }

        // Suchbegriffe werden mit Leerzeichen zusammengebaut
        // → z.B. "Tomate Basilikum" für die Begriffe ["Tomate", "Basilikum"]
        String joined = String.join(" ", cleanedTerms);

        // HTTP-Anfrage
        String responseBody = recipeAPIParser.requestRecipeAPI(joined, configService.getRecipeApiBase());

        // API-Antwort wird analysiert, nach Suchbegriffen gefiltert und abhängig von 'mode' verarbeitet
        return recipeAPIParser.parseRecipeTitlesFiltered(responseBody, cleanedTerms, mode);
    }

    public String getRecipeDetails(String recipeTitle) throws IOException, InterruptedException {
        // Delegate to the structured fetch, then format for display.
        RecipeDto data = fetchRecipeData(recipeTitle);
        return recipeAPIParser.formatRecipeDetailsFromData(data);
    }

    // Was macht diese Funktion? Sie holt die vollständigen Rezeptdaten (Beschreibung, Zutatenliste) für einen gegebenen
    // Rezepttitel.
    public RecipeDto fetchRecipeData(String recipeTitle) throws IOException, InterruptedException {
        // Methode: HTTP-Anfrage
        String responseBody = recipeAPIParser.requestRecipeAPI(recipeTitle, configService.getRecipeApiBase());

        String description = recipeAPIParser.getRecipeDescription(recipeTitle, responseBody);
        List<RecipeIngredientDto> ingredients = recipeAPIParser.parseIngredientsList(responseBody);

        String decodedDescription = recipeAPIParser.decodeUnicode(description);
        return new RecipeDto(null, recipeTitle, decodedDescription, ingredients);
    }
}
