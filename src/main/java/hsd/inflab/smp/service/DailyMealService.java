package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.DailyMealRequestDto;
import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.mapper.DailyMealMapper;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
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
    private final UserRepository userRepository;

    public DailyMealService(
            DailyMealRepository dailyMealRepo,
            RecipeRepository recipeRepo,
            DailyMealMapper dailyMealMapper,
            UserRepository userRepository) {
        this.dailyMealRepo = dailyMealRepo;
        this.recipeRepo = recipeRepo;
        this.dailyMealMapper = dailyMealMapper;
        this.userRepository = userRepository;
    }

    // --- LESE-METHODEN (Pures REST-Design) ---

    // Holt ALLE Pläne (Achtung: Kann später groß werden, dann evtl. paginieren)
    public List<DailyMealResponseDto> getAllMealPlans(String username) {
        return dailyMealMapper.toDtoList(dailyMealRepo.findByOwner(requireUser(username)));
    }

    public List<DailyMealResponseDto> getMealPlans(LocalDate start, LocalDate end, String username) {
        if (start == null && end == null) {
            return getAllMealPlans(username);
        }
        if (start != null && end == null) {
            return getMealPlansBetween(start, LocalDate.now(), username);
        }
        if (start == null) {
            return dailyMealRepo
                    .findFirstByOwnerOrderByMealDateAsc(requireUser(username))
                    .map(firstMeal -> getMealPlansBetween(firstMeal.getMealDate(), end, username))
                    .orElse(List.of());
        }
        return getMealPlansBetween(start, end, username);
    }

    // Holt EINEN Plan für ein spezielles Datum
    public DailyMealResponseDto getMealPlanByDate(LocalDate date, String username) {
        return findMealPlanByDate(date, username).orElse(null);
    }

    public Optional<DailyMealResponseDto> findMealPlanByDate(LocalDate date, String username) {
        return dailyMealRepo.findByOwnerAndMealDate(requireUser(username), date).map(dailyMealMapper::toDto);
    }

    // Holt eine Liste von Plänen für einen bestimmten Zeitraum (z.B. diese Woche)
    public List<DailyMealResponseDto> getMealPlansBetween(LocalDate start, LocalDate end, String username) {
        return dailyMealMapper.toDtoList(
                dailyMealRepo.findByOwnerAndMealDateBetween(requireUser(username), start, end));
    }

    public Map<LocalDate, DailyMealResponseDto> getMealPlansMapBetween(
            LocalDate start, LocalDate end, String username) {
        List<DailyMealResponseDto> mealplans = getMealPlansBetween(start, end, username);
        Map<LocalDate, DailyMealResponseDto> mealMap =
                mealplans.stream().collect(Collectors.toMap(DailyMealResponseDto::date, m -> m));
        return mealMap;
    }

    // --- SCHREIB-METHODEN ---

    public DailyMealResponseDto saveOrUpdateDailyMeal(DailyMealRequestDto dto, String username) {
        User owner = requireUser(username);
        // Sucht in der Datenbank nach einem bestehenden Eintrag für das Datum, wenn keiner gefunden wird, wird ein
        // neues Entity erstellt
        DailyMeal entity =
                dailyMealRepo.findByOwnerAndMealDate(owner, dto.date()).orElse(new DailyMeal());
        entity.setOwner(owner);
        entity.setGlobal(false);

        // Werte werden vom DTO ins das Datenbank-Objekt (Entity) kopiert. Beim Update werden alte Werte überschrieben.
        entity.setMealDate(dto.date());
        entity.setBreakfastServings(dto.breakfastServings());
        entity.setLunchServings(dto.lunchServings());
        entity.setDinnerServings(dto.dinnerServings());

        // Rezepte werden nur per ID referenziert. Wenn die übermittelte Rezept-ID 'null' ist, wird 'null' gesetzt;
        // existiert die ID nicht, wirft die fetch-Methode eine Exception.
        entity.setBreakfastRecipe(fetchRecipeSafely(dto.breakfastRecipeId(), owner));
        entity.setLunchRecipe(fetchRecipeSafely(dto.lunchRecipeId(), owner));
        entity.setDinnerRecipe(fetchRecipeSafely(dto.dinnerRecipeId(), owner));

        // Spring Boot JPA: Wenn Entity bereits vorhanden -> Update, wenn nicht vorhanden -> Insert. Rückgabe ist immer
        // das gespeicherte Entity mit ID (auch bei Update).
        DailyMeal savedMeal = dailyMealRepo.save(entity);
        // Das gespeicherte Ergebnis wird über den Mapper wieder in ein handliches 'DailyMealResponseDto' umgewandelt.
        return dailyMealMapper.toDto(savedMeal);
    }

    // --- HILFSMETHODEN (Geschäftslogik: Auflösung der Rezept-Referenz) ---

    // Only recipes the user may see can be planned; a foreign private recipe behaves as if it did not exist.
    private Recipe fetchRecipeSafely(UUID recipeId, User owner) {
        if (recipeId == null) {
            return null;
        }
        return recipeRepo
                .findVisibleById(recipeId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Rezept nicht gefunden!"));
    }

    private User requireUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }
}
