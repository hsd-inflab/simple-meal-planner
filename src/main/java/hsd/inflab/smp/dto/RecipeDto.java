package hsd.inflab.smp.dto;

import java.util.List;
import java.util.UUID;

public record RecipeDto(UUID id, String name, String description, List<RecipeIngredientDto> ingredientsPerPerson) {
    @Override
    public String toString() {
        return name();
    }
}
