package hsd.inflab.smp.dto.response;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.util.UUID;

public record RecipeIngredientResponseDto(
        UUID id, String name, Unit unit, Double amount, Category category, String foodType, String preparation) {
    @Override
    public String toString() {
        return name();
    }
}
