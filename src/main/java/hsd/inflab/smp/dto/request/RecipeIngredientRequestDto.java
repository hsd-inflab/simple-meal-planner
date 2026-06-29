package hsd.inflab.smp.dto.request;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;

/**
 * Request-DTO für eine Rezeptzutat (ohne {@code id} – die ID wird serverseitig vergeben).
 */
public record RecipeIngredientRequestDto(
        String name, Unit unit, Double amount, Category category, String foodType, String preparation) {}
