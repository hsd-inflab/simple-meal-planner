package hsd.inflab.smp.dto.response;

import hsd.inflab.smp.util.DateFormatUtil;
import java.time.LocalDate;
import java.util.UUID;

public record DailyMealResponseDto(
        UUID id,
        LocalDate date,
        RecipeResponseDto breakfastRecipe,
        RecipeResponseDto lunchRecipe,
        RecipeResponseDto dinnerRecipe,
        int breakfastServings,
        int lunchServings,
        int dinnerServings) {
    @Override
    public String toString() {
        return DateFormatUtil.formatAsGermanDate(date());
    }
}
