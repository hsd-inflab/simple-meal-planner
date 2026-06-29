package hsd.inflab.smp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyMealMapperTest {

    private final DailyMealMapper mapper = new DailyMealMapperImpl(new RecipeMapperImpl());

    @Test
    void toDto_mapsDateServingsAndExistingRecipes() {
        LocalDate date = LocalDate.of(2026, 4, 26);
        DailyMeal entity = new DailyMeal();
        entity.setMealDate(date);
        entity.setBreakfastRecipe(new Recipe("Fruehstueck", "", List.of()));
        entity.setLunchRecipe(null);
        entity.setDinnerRecipe(new Recipe("Abendessen", "", List.of()));
        entity.setBreakfastServings(2);
        entity.setLunchServings(0);
        entity.setDinnerServings(3);

        DailyMealResponseDto result = mapper.toDto(entity);

        // mealDate -> date Mapping
        assertEquals(date, result.date());
        assertEquals(2, result.breakfastServings());
        assertEquals(3, result.dinnerServings());
        assertNotNull(result.breakfastRecipe());
        assertEquals("Fruehstueck", result.breakfastRecipe().name());
        assertNull(result.lunchRecipe());
        assertNotNull(result.dinnerRecipe());
        assertEquals("Abendessen", result.dinnerRecipe().name());
    }

    @Test
    void toDtoList_mapsAllEntries() {
        DailyMeal one = new DailyMeal();
        one.setMealDate(LocalDate.of(2026, 4, 28));
        DailyMeal two = new DailyMeal();
        two.setMealDate(LocalDate.of(2026, 4, 29));

        List<DailyMealResponseDto> result = mapper.toDtoList(List.of(one, two));

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 4, 28), result.get(0).date());
        assertEquals(LocalDate.of(2026, 4, 29), result.get(1).date());
    }

    @Test
    void mappingNullReturnsNull() {
        assertNull(mapper.toDto(null));
    }
}
