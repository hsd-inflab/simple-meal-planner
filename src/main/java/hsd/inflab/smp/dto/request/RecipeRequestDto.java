package hsd.inflab.smp.dto.request;

import java.util.List;

/**
 * Request-DTO für ein Rezept (ohne {@code id} – die ID wird serverseitig vergeben).
 */
public record RecipeRequestDto(
        String name, String description, List<RecipeIngredientRequestDto> ingredientsPerPerson) {}
