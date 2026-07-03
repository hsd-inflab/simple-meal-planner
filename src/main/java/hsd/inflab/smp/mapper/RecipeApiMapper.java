package hsd.inflab.smp.mapper;

import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeIngredientDto;
import hsd.inflab.smp.dto.response.RecipeIngredientResponseDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecipeApiMapper {

    /*
     * Beide ExternalDto's werden in die RecipeResponseDto überführt
     *
     * ExternalRecipeDto: title, source, keywords, ingredients
     * ExternalCrawlRecipeDto: steps
     */
    public RecipeResponseDto toRecipeDto(ExternalRecipeDto searchRecipe, ExternalCrawlRecipeDto crawlRecipe) {
        return new RecipeResponseDto(
                null,
                searchRecipe.title(),
                mapStepsToDescription(crawlRecipe.steps()),
                mapIngredients(searchRecipe.ingredients()));
    }

    // Fallback, falls der Crawl-Abruf fehlschlägt oder nicht durchgeführt wird
    public RecipeResponseDto toRecipeDto(ExternalRecipeDto externalRecipe) {
        return new RecipeResponseDto(null, externalRecipe.title(), null, mapIngredients(externalRecipe.ingredients()));
    }

    private List<RecipeIngredientResponseDto> mapIngredients(List<ExternalRecipeIngredientDto> externalIngredients) {
        if (externalIngredients == null) {
            return List.of();
        }

        return externalIngredients.stream().map(this::mapIngredient).toList();
    }

    private RecipeIngredientResponseDto mapIngredient(ExternalRecipeIngredientDto externalIngredient) {
        return new RecipeIngredientResponseDto(
                UUID.randomUUID(),
                externalIngredient.name(),
                mapUnit(externalIngredient.unit()),
                mapAmount(externalIngredient.amount()),
                null,
                null,
                null);
    }

    private String mapStepsToDescription(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }

        return String.join("\n\n", steps);
    }

    private Double mapAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            return 0.0;
        }

        try {
            return Double.valueOf(amount.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Unit mapUnit(String apiUnit) {
        // Falls die API keinen Wert liefert oder nur Leerzeichen enthält, geben wir Unit.NONE zurück
        if (apiUnit == null || apiUnit.isBlank()) {
            return Unit.NONE;
        }

        // Vereinheitlichung aller API-Werte: Entfernen von führenden und nachfolgenden Leerzeichen, Umwandlung in
        // Kleinbuchstaben
        String normalizedApiUnit = apiUnit.trim().toLowerCase(Locale.ROOT);

        return Unit.getApiLookupMap(Locale.GERMAN).entrySet().stream() // Alle Einträge der Map werden durchlaufen
                .filter(entry -> entry.getKey()
                        != null) // Map-Einträge werden ignoriert, deren Key 'null' ist (Sicherheitsabfrage)
                .filter(entry -> entry.getKey() // Vergleich von Key aus der Properties-Datei mit dem API-Wert
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .equals(normalizedApiUnit))
                .map(Map.Entry::getValue) // Passender Eintrag gefunden? Wert der Map wird genommen
                .findFirst() // ersten passenden Treffer
                .orElse(Unit.NONE); // Falls kein passender Eintrag gefunden wurde, wird Unit.NONE zurückgegeben
    }
}
