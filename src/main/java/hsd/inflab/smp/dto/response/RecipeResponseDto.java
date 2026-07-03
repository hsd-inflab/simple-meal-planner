package hsd.inflab.smp.dto.response;

import java.util.List;
import java.util.UUID;

public record RecipeResponseDto(
        UUID id, String name, String description, List<RecipeIngredientResponseDto> ingredientsPerPerson) {
    @Override
    public String toString() {
        return name();
    }
}
