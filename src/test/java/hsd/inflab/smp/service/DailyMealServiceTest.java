package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyMealServiceTest {

    @Mock
    private DailyMealRepository dailyMealRepo;

    @Mock
    private RecipeRepository recipeRepo;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private DailyMealService dailyMealService;

    @Test
    void saveOrUpdateDailyMeal_inserts_whenDateNotExists() {
        // Zufälliges Datums
        LocalDate date = LocalDate.of(2026, 4, 21);

        // Zufällige UUID
        UUID breakfastId = UUID.randomUUID();
        RecipeDto breakfastDto = new RecipeDto(breakfastId, "Omelett", "", null);
        DailyMealDto input = new DailyMealDto(null, date, breakfastDto, null, null, 2, 0, 0);

        Recipe breakfastEntity = new Recipe();
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());
        when(recipeRepo.findById(breakfastId)).thenReturn(Optional.of(breakfastEntity));
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeService.convertToDto(breakfastEntity)).thenReturn(breakfastDto);

        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        assertEquals(date, result.date());
        assertEquals(2, result.breakfastServings());
        assertEquals(breakfastId, result.breakfastRecipe().id());
        verify(dailyMealRepo).save(any(DailyMeal.class));
    }

    @Test
    void saveOrUpdateDailyMeal_updates_whenDateExists() {
        LocalDate date = LocalDate.of(2026, 4, 22);
        DailyMeal existing = new DailyMeal();
        existing.setMealDate(date);

        DailyMealDto input = new DailyMealDto(null, date, null, null, null, 1, 2, 3);
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.of(existing));
        when(dailyMealRepo.save(existing)).thenReturn(existing);

        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        assertEquals(1, result.breakfastServings());
        assertEquals(2, result.lunchServings());
        assertEquals(3, result.dinnerServings());
        verify(dailyMealRepo).save(existing);
    }

    @Test
    void saveOrUpdateDailyMeal_setsNullRecipes_whenDtoRecipesNull() {
        LocalDate date = LocalDate.of(2026, 4, 23);
        DailyMealDto input = new DailyMealDto(null, date, null, null, null, 1, 1, 1);

        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        assertNull(result.breakfastRecipe());
        assertNull(result.lunchRecipe());
        assertNull(result.dinnerRecipe());
        verify(recipeRepo, never()).findById(any(UUID.class));
    }

    @Test
    void saveOrUpdateDailyMeal_throws_whenRecipeMissing() {
        LocalDate date = LocalDate.of(2026, 4, 24);
        UUID missingId = UUID.randomUUID();
        RecipeDto missingDto = new RecipeDto(missingId, "Unbekannt", "", null);
        DailyMealDto input = new DailyMealDto(null, date, missingDto, null, null, 1, 0, 0);

        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());
        when(recipeRepo.findById(missingId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> dailyMealService.saveOrUpdateDailyMeal(input));

        assertTrue(ex.getMessage().contains("Rezept nicht gefunden"));
        verify(dailyMealRepo, never()).save(any(DailyMeal.class));
    }

    @Test
    void saveOrUpdateDailyMeal_setsNullRecipe_whenRecipeIdNull() {
        LocalDate date = LocalDate.of(2026, 4, 25);
        RecipeDto dtoWithoutId = new RecipeDto(null, "Ohne ID", "", null);
        DailyMealDto input = new DailyMealDto(null, date, dtoWithoutId, null, null, 1, 0, 0);

        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        assertNull(result.breakfastRecipe());
        verify(recipeRepo, never()).findById(any(UUID.class));
    }

    @Test
    void getMealPlanByDate_convertsOnlyExistingRecipes() {
        LocalDate date = LocalDate.of(2026, 4, 26);
        Recipe breakfast = new Recipe();
        Recipe dinner = new Recipe();
        RecipeDto breakfastDto = new RecipeDto(UUID.randomUUID(), "Fruehstueck", "", null);
        RecipeDto dinnerDto = new RecipeDto(UUID.randomUUID(), "Abendessen", "", null);

        DailyMeal entity = new DailyMeal();
        entity.setMealDate(date);
        entity.setBreakfastRecipe(breakfast);
        entity.setLunchRecipe(null);
        entity.setDinnerRecipe(dinner);
        entity.setBreakfastServings(2);
        entity.setLunchServings(0);
        entity.setDinnerServings(3);

        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.of(entity));
        when(recipeService.convertToDto(breakfast)).thenReturn(breakfastDto);
        when(recipeService.convertToDto(dinner)).thenReturn(dinnerDto);

        DailyMealDto result = dailyMealService.getMealPlanByDate(date);

        assertEquals(date, result.date());
        assertEquals(breakfastDto, result.breakfastRecipe());
        assertNull(result.lunchRecipe());
        assertEquals(dinnerDto, result.dinnerRecipe());
        verify(recipeService, times(2)).convertToDto(any(Recipe.class));
    }

    @Test
    void getMealPlanByDate_returnsNull_whenNotFound() {
        LocalDate date = LocalDate.of(2026, 4, 27);
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        DailyMealDto result = dailyMealService.getMealPlanByDate(date);

        assertNull(result);
    }

    @Test
    void getMealPlansMapBetween_returnsDateKeyedMap() {
        LocalDate start = LocalDate.of(2026, 4, 28);
        LocalDate end = LocalDate.of(2026, 4, 29);

        DailyMeal dayOne = new DailyMeal();
        dayOne.setMealDate(start);
        dayOne.setBreakfastServings(1);
        dayOne.setLunchServings(1);
        dayOne.setDinnerServings(1);

        DailyMeal dayTwo = new DailyMeal();
        dayTwo.setMealDate(end);
        dayTwo.setBreakfastServings(2);
        dayTwo.setLunchServings(2);
        dayTwo.setDinnerServings(2);

        when(dailyMealRepo.findByMealDateBetween(start, end)).thenReturn(List.of(dayOne, dayTwo));

        Map<LocalDate, DailyMealDto> result = dailyMealService.getMealPlansMapBetween(start, end);

        assertEquals(2, result.size());
        assertEquals(1, result.get(start).breakfastServings());
        assertEquals(2, result.get(end).dinnerServings());
    }
}
