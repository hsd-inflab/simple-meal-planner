package hsd.inflab.smp.service;

import hsd.inflab.smp.client.RecipeApiClient;
import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.enums.SearchMode;
import hsd.inflab.smp.mapper.RecipeApiMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Dienst zum Abrufen von Rezepten aus dem Web
 */
@Service
public class RecipeAPIService {

    private final RecipeApiClient recipeApiClient;

    private final RecipeApiMapper recipeApiMapper;

    public RecipeAPIService(RecipeApiClient recipeApiClient, RecipeApiMapper recipeApiMapper) {
        this.recipeApiClient = recipeApiClient;
        this.recipeApiMapper = recipeApiMapper;
    }

    public List<String> searchRecipeTitles(List<String> terms, SearchMode mode) {
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

        List<ExternalRecipeDto> externalRecipes = recipeApiClient.searchRecipes(joined);

        if (externalRecipes == null || externalRecipes.isEmpty()) {
            return List.of();
        }

        return externalRecipes.stream()
                .map(ExternalRecipeDto::title)
                .filter(title -> title != null && !title.isBlank())
                .distinct()
                .toList();
    }

    public RecipeResponseDto getRecipeDetails(String recipeTitle) {
        return fetchRecipeData(recipeTitle);
    }

    public RecipeResponseDto fetchRecipeData(String recipeTitle) {
        if (recipeTitle == null || recipeTitle.isBlank()) {
            return null;
        }

        List<ExternalRecipeDto> searchResults = recipeApiClient.searchRecipes(recipeTitle);

        if (searchResults == null || searchResults.isEmpty()) {
            return null;
        }

        ExternalRecipeDto matchingRecipe = searchResults.stream()
                .filter(recipe -> recipe.title() != null)
                .filter(recipe -> recipe.title().equalsIgnoreCase(recipeTitle))
                .findFirst()
                .orElse(searchResults.get(0));

        if (matchingRecipe.source() == null || matchingRecipe.source().isBlank()) {
            return recipeApiMapper.toRecipeDto(matchingRecipe);
        }

        ExternalCrawlRecipeDto crawledRecipe = recipeApiClient.crawlRecipe(matchingRecipe.source());

        if (crawledRecipe == null) {
            return recipeApiMapper.toRecipeDto(matchingRecipe);
        }

        return recipeApiMapper.toRecipeDto(matchingRecipe, crawledRecipe);
    }
}
