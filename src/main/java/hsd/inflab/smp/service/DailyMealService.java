package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.DailyMealRequestDto;
import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.mapper.DailyMealMapper;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DailyMealService {

    private final DailyMealRepository dailyMealRepo;
    private final RecipeRepository recipeRepo;
    private final DailyMealMapper dailyMealMapper;

    public DailyMealService(
            DailyMealRepository dailyMealRepo, RecipeRepository recipeRepo, DailyMealMapper dailyMealMapper) {
        this.dailyMealRepo = dailyMealRepo;
        this.recipeRepo = recipeRepo;
        this.dailyMealMapper = dailyMealMapper;
    }

    // --- LESE-METHODEN (Pures REST-Design) ---

    // Holt ALLE Pläne (Achtung: Kann später groß werden, dann evtl. paginieren)
    public List<DailyMealResponseDto> getAllMealPlans() {
        return dailyMealMapper.toDtoList(dailyMealRepo.findAll());
    }

    public List<DailyMealResponseDto> getMealPlans(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return getAllMealPlans();
        }
        if (start != null && end == null) {
            return getMealPlansBetween(start, LocalDate.now());
        }
        if (start == null) {
            return dailyMealRepo
                    .findFirstByOrderByMealDateAsc()
                    .map(firstMeal -> getMealPlansBetween(firstMeal.getMealDate(), end))
                    .orElse(List.of());
        }
        return getMealPlansBetween(start, end);
    }

    // Holt EINEN Plan für ein spezielles Datum
    public DailyMealResponseDto getMealPlanByDate(LocalDate date) {
        return dailyMealRepo.findByMealDate(date).map(dailyMealMapper::toDto).orElse(null);
    }

    public Optional<DailyMealResponseDto> findMealPlanByDate(LocalDate date) {
        return dailyMealRepo.findByMealDate(date).map(dailyMealMapper::toDto);
    }

    // Holt eine Liste von Plänen für einen bestimmten Zeitraum (z.B. diese Woche)
    public List<DailyMealResponseDto> getMealPlansBetween(LocalDate start, LocalDate end) {
        return dailyMealMapper.toDtoList(dailyMealRepo.findByMealDateBetween(start, end));
    }

    public Map<LocalDate, DailyMealResponseDto> getMealPlansMapBetween(LocalDate start, LocalDate end) {
        List<DailyMealResponseDto> mealplans = getMealPlansBetween(start, end);
        Map<LocalDate, DailyMealResponseDto> mealMap =
                mealplans.stream().collect(Collectors.toMap(DailyMealResponseDto::date, m -> m));
        return mealMap;
    }

    // --- SCHREIB-METHODEN ---

    public DailyMealResponseDto saveOrUpdateDailyMeal(DailyMealRequestDto dto) {
        // Upsert-Logik bleibt erhalten, da die DB das Datum unique hält!
        // Sucht in der Datenbank nach einem bestehenden Eintrag für das Datum, wenn keiner gefunden wird, wird ein
        // neues Entity erstellt
        DailyMeal entity = dailyMealRepo.findByMealDate(dto.date()).orElse(new DailyMeal());

        // Werte werden vom DTO ins das Datenbank-Objekt (Entity) kopiert. Beim Update werden alte Werte überschrieben.
        entity.setMealDate(dto.date());
        entity.setBreakfastServings(dto.breakfastServings());
        entity.setLunchServings(dto.lunchServings());
        entity.setDinnerServings(dto.dinnerServings());

        // Rezepte werden nur per ID referenziert. Wenn die übermittelte Rezept-ID 'null' ist, wird 'null' gesetzt;
        // existiert die ID nicht, wirft die fetch-Methode eine Exception.
        entity.setBreakfastRecipe(fetchRecipeSafely(dto.breakfastRecipeId()));
        entity.setLunchRecipe(fetchRecipeSafely(dto.lunchRecipeId()));
        entity.setDinnerRecipe(fetchRecipeSafely(dto.dinnerRecipeId()));

        // Spring Boot JPA: Wenn Entity bereits vorhanden -> Update, wenn nicht vorhanden -> Insert. Rückgabe ist immer
        // das gespeicherte Entity mit ID (auch bei Update).
        DailyMeal savedMeal = dailyMealRepo.save(entity);
        // Das gespeicherte Ergebnis wird über den Mapper wieder in ein handliches 'DailyMealResponseDto' umgewandelt.
        return dailyMealMapper.toDto(savedMeal);
    }

    // --- HILFSMETHODEN (Geschäftslogik: Auflösung der Rezept-Referenz) ---

    private Recipe fetchRecipeSafely(UUID recipeId) {
        if (recipeId == null) {
            return null;
        }
        return recipeRepo.findById(recipeId).orElseThrow(() -> new IllegalArgumentException("Rezept nicht gefunden!"));
    }
}
