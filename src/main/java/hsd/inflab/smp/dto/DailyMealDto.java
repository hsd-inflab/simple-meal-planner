package hsd.inflab.smp.dto;

import hsd.inflab.smp.util.DateFormatUtil;
import java.time.LocalDate;
import java.util.UUID;

public record DailyMealDto(
        UUID id,
        LocalDate date,
        RecipeDto breakfastRecipe,
        RecipeDto lunchRecipe,
        RecipeDto dinnerRecipe,
        int breakfastServings,
        int lunchServings,
        int dinnerServings) {
    @Override
    public String toString() {
        return DateFormatUtil.formatAsGermanDate(date());
    }
}
