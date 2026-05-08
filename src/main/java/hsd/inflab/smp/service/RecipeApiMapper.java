package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.dto.RecipeIngredientDto;
import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeIngredientDto;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecipeApiMapper {

    public RecipeDto toRecipeDto(ExternalRecipeDto externalRecipe) {
        return new RecipeDto(null, externalRecipe.title(), null, mapIngredients(externalRecipe.ingredients()));
    }

    public RecipeDto toRecipeDto(ExternalCrawlRecipeDto externalRecipe) {
        return new RecipeDto(
                null,
                externalRecipe.title(),
                mapStepsToDescription(externalRecipe.steps()),
                mapIngredients(externalRecipe.ingredients()));
    }

    private List<RecipeIngredientDto> mapIngredients(List<ExternalRecipeIngredientDto> externalIngredients) {
        if (externalIngredients == null) {
            return List.of();
        }

        return externalIngredients.stream().map(this::mapIngredient).toList();
    }

    private RecipeIngredientDto mapIngredient(ExternalRecipeIngredientDto externalIngredient) {
        return new RecipeIngredientDto(
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

    private Unit mapUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return Unit.NONE;
        }

        String normalizedUnit = unit.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedUnit) {
            case "ml" -> Unit.ML;
            case "cl" -> Unit.CL;
            case "dl" -> Unit.DL;
            case "l" -> Unit.L;
            case "g" -> Unit.G;
            case "kg" -> Unit.KG;
            case "tl" -> Unit.TSP;
            case "el" -> Unit.TBSP;
            case "tasse" -> Unit.CUP;
            case "prise" -> Unit.PINCH;
            case "stk.", "stk", "stück" -> Unit.UNIT;
            default -> Unit.NONE;
        };
    }
}
