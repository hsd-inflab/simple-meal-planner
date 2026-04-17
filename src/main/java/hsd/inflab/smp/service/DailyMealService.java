package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DailyMealService {

    private final DailyMealRepository dailyMealRepo;
    private final RecipeRepository recipeRepo;
    private final RecipeService recipeService;

    public DailyMealService(
            DailyMealRepository dailyMealRepo, RecipeRepository recipeRepo, RecipeService recipeService) {
        this.dailyMealRepo = dailyMealRepo;
        this.recipeRepo = recipeRepo;
        this.recipeService = recipeService;
    }

    // --- LESE-METHODEN (Pures REST-Design) ---

    // Holt ALLE Pläne (Achtung: Kann später groß werden, dann evtl. paginieren)
    public List<DailyMealDto> getAllMealPlans() {
        return dailyMealRepo.findAll().stream().map(this::convertToDto).toList();
    }

    // Holt EINEN Plan für ein spezielles Datum
    public DailyMealDto getMealPlanByDate(LocalDate date) {
        return dailyMealRepo.findByMealDate(date).map(this::convertToDto).orElse(null);
    }

    // Holt eine Liste von Plänen für einen bestimmten Zeitraum (z.B. diese Woche)
    public List<DailyMealDto> getMealPlansBetween(LocalDate start, LocalDate end) {
        return dailyMealRepo.findByMealDateBetween(start, end).stream()
                .map(this::convertToDto)
                .toList();
    }

    public Map<LocalDate, DailyMealDto> getMealPlansMapBetween(LocalDate start, LocalDate end) {
        List<DailyMealDto> mealplans = getMealPlansBetween(start, end);
        Map<LocalDate, DailyMealDto> mealMap = mealplans.stream().collect(Collectors.toMap(DailyMealDto::date, m -> m));
        return mealMap;
    }

    // --- SCHREIB-METHODEN ---

    public DailyMealDto saveOrUpdateDailyMeal(DailyMealDto dto) {
        // Upsert-Logik bleibt erhalten, da die DB das Datum unique hält!
        DailyMeal entity = dailyMealRepo.findByMealDate(dto.date()).orElse(new DailyMeal());

        entity.setMealDate(dto.date());
        entity.setBreakfastServings(dto.breakfastServings());
        entity.setLunchServings(dto.lunchServings());
        entity.setDinnerServings(dto.dinnerServings());

        entity.setBreakfastRecipe(fetchRecipeSafely(dto.breakfastRecipe()));
        entity.setLunchRecipe(fetchRecipeSafely(dto.lunchRecipe()));
        entity.setDinnerRecipe(fetchRecipeSafely(dto.dinnerRecipe()));

        DailyMeal savedMeal = dailyMealRepo.save(entity);
        return convertToDto(savedMeal);
    }

    // --- HILFSMETHODEN (Bleiben exakt gleich) ---

    private Recipe fetchRecipeSafely(RecipeDto recipeDto) {
        if (recipeDto == null || recipeDto.id() == null) {
            return null;
        }
        return recipeRepo
                .findById(recipeDto.id())
                .orElseThrow(() -> new IllegalArgumentException("Rezept nicht gefunden!"));
    }

    private DailyMealDto convertToDto(DailyMeal entity) {
        return new DailyMealDto(
                entity.getId(),
                entity.getMealDate(),
                entity.getBreakfastRecipe() != null ? recipeService.convertToDto(entity.getBreakfastRecipe()) : null,
                entity.getLunchRecipe() != null ? recipeService.convertToDto(entity.getLunchRecipe()) : null,
                entity.getDinnerRecipe() != null ? recipeService.convertToDto(entity.getDinnerRecipe()) : null,
                entity.getBreakfastServings(),
                entity.getLunchServings(),
                entity.getDinnerServings());
    }
}
