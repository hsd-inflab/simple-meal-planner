package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DailyMealServiceTest {

    private DailyMealRepository dailyMealRepository;
    private DailyMealService dailyMealService;

    @BeforeEach
    void setUp() {
        dailyMealRepository = Mockito.mock(DailyMealRepository.class);
        RecipeRepository recipeRepository = Mockito.mock(RecipeRepository.class);
        RecipeService recipeService = Mockito.mock(RecipeService.class);
        dailyMealService = new DailyMealService(dailyMealRepository, recipeRepository, recipeService);
    }

    @Test
    void getMealPlansWithoutParametersReturnsAllMealPlans() {
        DailyMeal firstMeal = new DailyMeal();
        firstMeal.setMealDate(LocalDate.of(2026, 4, 20));
        DailyMeal secondMeal = new DailyMeal();
        secondMeal.setMealDate(LocalDate.of(2026, 4, 21));
        when(dailyMealRepository.findAll()).thenReturn(List.of(firstMeal, secondMeal));

        List<DailyMealDto> result = dailyMealService.getMealPlans(null, null);

        assertEquals(2, result.size());
        verify(dailyMealRepository).findAll();
    }

    @Test
    void getMealPlansWithOnlyStartUsesTodayAsEnd() {
        LocalDate start = LocalDate.of(2026, 4, 20);
        LocalDate today = LocalDate.now();
        DailyMeal firstMeal = new DailyMeal();
        firstMeal.setMealDate(start);
        when(dailyMealRepository.findByMealDateBetween(start, today)).thenReturn(List.of(firstMeal));

        List<DailyMealDto> result = dailyMealService.getMealPlans(start, null);

        assertEquals(List.of(start), result.stream().map(DailyMealDto::date).toList());
        verify(dailyMealRepository).findByMealDateBetween(start, today);
    }

    @Test
    void getMealPlansWithOnlyEndUsesFirstExistingMealAsStart() {
        LocalDate firstDate = LocalDate.of(2026, 4, 18);
        LocalDate end = LocalDate.of(2026, 4, 22);
        DailyMeal firstMeal = new DailyMeal();
        firstMeal.setMealDate(firstDate);
        DailyMeal resultMeal = new DailyMeal();
        resultMeal.setMealDate(LocalDate.of(2026, 4, 20));
        when(dailyMealRepository.findFirstByOrderByMealDateAsc()).thenReturn(java.util.Optional.of(firstMeal));
        when(dailyMealRepository.findByMealDateBetween(firstDate, end)).thenReturn(List.of(resultMeal));

        List<DailyMealDto> result = dailyMealService.getMealPlans(null, end);

        assertEquals(List.of(LocalDate.of(2026, 4, 20)), result.stream().map(DailyMealDto::date).toList());
        verify(dailyMealRepository).findFirstByOrderByMealDateAsc();
        verify(dailyMealRepository).findByMealDateBetween(firstDate, end);
    }

    @Test
    void getMealPlansWithOnlyEndReturnsEmptyListWhenNoMealsExist() {
        LocalDate end = LocalDate.of(2026, 4, 22);
        when(dailyMealRepository.findFirstByOrderByMealDateAsc()).thenReturn(java.util.Optional.empty());

        List<DailyMealDto> result = dailyMealService.getMealPlans(null, end);

        assertEquals(List.of(), result);
        verify(dailyMealRepository).findFirstByOrderByMealDateAsc();
    }

    @Test
    void getMealPlansWithStartAndEndUsesExactRange() {
        LocalDate start = LocalDate.of(2026, 4, 20);
        LocalDate end = LocalDate.of(2026, 4, 26);
        DailyMeal meal = new DailyMeal();
        meal.setMealDate(LocalDate.of(2026, 4, 24));
        when(dailyMealRepository.findByMealDateBetween(start, end)).thenReturn(List.of(meal));

        List<DailyMealDto> result = dailyMealService.getMealPlans(start, end);

        assertEquals(List.of(LocalDate.of(2026, 4, 24)), result.stream().map(DailyMealDto::date).toList());
        verify(dailyMealRepository).findByMealDateBetween(start, end);
    }
}
