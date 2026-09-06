package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.request.DailyMealRequestDto;
import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.mapper.DailyMealMapper;
import hsd.inflab.smp.mapper.DailyMealMapperImpl;
import hsd.inflab.smp.mapper.RecipeMapperImpl;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit-Tests für {@link DailyMealService}.
 *
 * <p>Geprüft wird die Geschäftslogik des Services isoliert mit Mockito:
 * - Repository-Aufrufe werden gemockt
 * - Das Entity-&gt;DTO-Mapping übernimmt der echte {@link DailyMealMapper} (inkl. {@link RecipeMapperImpl})
 * - Insert, Update, Fehlerfälle und die Auflösung der Rezept-Referenzen
 */
@ExtendWith(MockitoExtension.class)
class DailyMealServiceTest {

    // Mock für den Zugriff auf DailyMeal-Daten aus der Datenbank
    @Mock
    private DailyMealRepository dailyMealRepo;

    // Mock für den Zugriff auf Rezepte aus der Datenbank
    @Mock
    private RecipeRepository recipeRepo;

    // Echter Mapper (mit echtem RecipeMapper), da das Mapping kein Mock-Seam mehr ist
    private final DailyMealMapper dailyMealMapper = new DailyMealMapperImpl(new RecipeMapperImpl());

    // Mock fuer die Aufloesung des angemeldeten Users
    @Mock
    private UserRepository userRepository;

    private static final String USERNAME = "meal-owner";

    private User owner;

    private DailyMealService dailyMealService;

    @BeforeEach
    void setUp() {
        owner = new User(USERNAME, "test-password-hash", List.of(Role.USER));
        dailyMealService = new DailyMealService(dailyMealRepo, recipeRepo, dailyMealMapper, userRepository);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(owner));
    }

    /**
     * Prüft den Insert-Fall:
     * Wenn für das Datum noch kein DailyMeal existiert, soll ein neuer Eintrag gespeichert werden.
     */
    @Test
    void saveOrUpdateDailyMeal_inserts_whenDateNotExists() {
        // Testdatum für den neuen Meal-Plan
        LocalDate date = LocalDate.of(2026, 4, 21);

        // Frühstücksrezept-Referenz mit zufälliger UUID
        UUID breakfastId = UUID.randomUUID();

        // Eingabe-DTO: Frühstück ist gesetzt, Mittag/Abend nicht
        DailyMealRequestDto input = new DailyMealRequestDto(date, breakfastId, null, null, 2, 0, 0);

        // Simuliertes Rezept-Entity aus der Datenbank (Name wird beim Mapping übernommen)
        Recipe breakfastEntity = new Recipe("Omelett", "", List.of());

        // Für dieses Datum existiert noch kein Meal-Plan
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.empty());

        // Das Rezept mit der UUID wird gefunden
        when(recipeRepo.findVisibleById(breakfastId, owner)).thenReturn(Optional.of(breakfastEntity));

        // Beim Speichern soll das übergebene Entity direkt zurückgegeben werden
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Service-Methode ausführen
        DailyMealResponseDto result = dailyMealService.saveOrUpdateDailyMeal(input, USERNAME);

        // Prüfen, ob die Daten korrekt übernommen wurden
        assertEquals(date, result.date());
        assertEquals(2, result.breakfastServings());
        assertNotNull(result.breakfastRecipe());
        assertEquals("Omelett", result.breakfastRecipe().name());

        // Sicherstellen, dass gespeichert wurde
        verify(dailyMealRepo).save(any(DailyMeal.class));
    }

    /**
     * Prüft den Update-Fall:
     * Wenn bereits ein DailyMeal für das Datum existiert, sollen die Werte überschrieben werden.
     */
    @Test
    void saveOrUpdateDailyMeal_updates_whenDateExists() {
        // Datum, für das bereits ein Eintrag vorhanden ist
        LocalDate date = LocalDate.of(2026, 4, 22);

        // Vorhandenes Entity aus der Datenbank
        DailyMeal existing = new DailyMeal();
        existing.setMealDate(date);

        // Neues DTO mit geänderten Portionszahlen
        DailyMealRequestDto input = new DailyMealRequestDto(date, null, null, null, 1, 2, 3);

        // Für das Datum existiert bereits ein Eintrag
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.of(existing));

        // Beim Speichern wird das vorhandene Entity zurückgegeben
        when(dailyMealRepo.save(existing)).thenReturn(existing);

        // Service aufrufen
        DailyMealResponseDto result = dailyMealService.saveOrUpdateDailyMeal(input, USERNAME);

        // Prüfen, ob die Portionszahlen korrekt übernommen wurden
        assertEquals(1, result.breakfastServings());
        assertEquals(2, result.lunchServings());
        assertEquals(3, result.dinnerServings());

        // Sicherstellen, dass genau dieses Entity gespeichert wurde
        verify(dailyMealRepo).save(existing);
    }

    /**
     * Prüft den Fall, dass im DTO keine Rezepte gesetzt sind.
     * Der Service soll dann keine Rezeptsuche durchführen und alle Rezeptfelder auf null lassen.
     */
    @Test
    void saveOrUpdateDailyMeal_setsNullRecipes_whenDtoRecipesNull() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 23);

        // DTO ohne Rezept-Referenzen
        DailyMealRequestDto input = new DailyMealRequestDto(date, null, null, null, 1, 1, 1);

        // Für das Datum existiert noch kein Eintrag
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.empty());

        // Beim Speichern wird das übergebene Entity direkt zurückgegeben
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Service aufrufen
        DailyMealResponseDto result = dailyMealService.saveOrUpdateDailyMeal(input, USERNAME);

        // Alle Rezeptfelder sollen null sein
        assertNull(result.breakfastRecipe());
        assertNull(result.lunchRecipe());
        assertNull(result.dinnerRecipe());

        // Es darf keine Rezeptsuche geben, weil im DTO keine Rezepte gesetzt waren
        verify(recipeRepo, never()).findById(any(UUID.class));
    }

    /**
     * Prüft den Fehlerfall:
     * Wenn eine Rezept-ID angegeben ist, das Rezept aber nicht existiert,
     * soll eine IllegalArgumentException geworfen werden.
     */
    @Test
    void saveOrUpdateDailyMeal_throws_whenRecipeMissing() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 24);

        // Zufällige Rezept-ID, die nicht gefunden wird
        UUID missingId = UUID.randomUUID();

        // DTO mit einer fehlenden Frühstücksrezept-Referenz
        DailyMealRequestDto input = new DailyMealRequestDto(date, missingId, null, null, 1, 0, 0);

        // Für das Datum existiert noch kein Eintrag
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.empty());

        // Die Rezeptsuche liefert nichts zurück
        when(recipeRepo.findVisibleById(missingId, owner)).thenReturn(Optional.empty());

        // Erwartet wird eine IllegalArgumentException
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> dailyMealService.saveOrUpdateDailyMeal(input, USERNAME));

        // Fehlermeldung soll den erwarteten Hinweis enthalten
        assertTrue(ex.getMessage().contains("Rezept nicht gefunden"));

        // Bei Fehler darf nicht gespeichert werden
        verify(dailyMealRepo, never()).save(any(DailyMeal.class));
    }

    /**
     * Prüft die Umwandlung von Entity zu DTO:
     * Nur vorhandene Rezepte sollen in DTOs konvertiert werden, fehlende bleiben null.
     */
    @Test
    void getMealPlanByDate_convertsOnlyExistingRecipes() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 26);

        // Zwei Rezept-Entities, die im Meal-Plan referenziert werden
        Recipe breakfast = new Recipe("Fruehstueck", "", List.of());
        Recipe dinner = new Recipe("Abendessen", "", List.of());

        // DailyMeal-Entity mit Frühstück und Abendessen, aber ohne Mittagessen
        DailyMeal entity = new DailyMeal();
        entity.setMealDate(date);
        entity.setBreakfastRecipe(breakfast);
        entity.setLunchRecipe(null);
        entity.setDinnerRecipe(dinner);
        entity.setBreakfastServings(2);
        entity.setLunchServings(0);
        entity.setDinnerServings(3);

        // Repository liefert das gespeicherte Entity
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.of(entity));

        // Service aufrufen
        DailyMealResponseDto result = dailyMealService.getMealPlanByDate(date, USERNAME);

        // Prüfen, ob Datum und Rezeptzuordnung korrekt sind
        assertEquals(date, result.date());
        assertNotNull(result.breakfastRecipe());
        assertEquals("Fruehstueck", result.breakfastRecipe().name());
        assertNull(result.lunchRecipe());
        assertNotNull(result.dinnerRecipe());
        assertEquals("Abendessen", result.dinnerRecipe().name());
    }

    /**
     * Prüft den "nicht gefunden"-Fall:
     * Wenn für das Datum kein Mealplan existiert, soll null zurückgegeben werden.
     */
    @Test
    void getMealPlanByDate_returnsNull_whenNotFound() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 27);

        // Repository findet keinen Datensatz
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.empty());

        // Service aufrufen
        DailyMealResponseDto result = dailyMealService.getMealPlanByDate(date, USERNAME);

        // Erwartet: null, weil kein Mealplan existiert
        assertNull(result);
    }

    /**
     * Prüft die Zeitraum-Abfrage:
     * Die zurückgegebenen Mealplans sollen in einer Map mit LocalDate als Schlüssel landen.
     */
    @Test
    void getMealPlansMapBetween_returnsDateKeyedMap() {
        // Zeitraum definieren
        LocalDate start = LocalDate.of(2026, 4, 28);
        LocalDate end = LocalDate.of(2026, 4, 29);

        // Erster Tagesplan
        DailyMeal dayOne = new DailyMeal();
        dayOne.setMealDate(start);
        dayOne.setBreakfastServings(1);
        dayOne.setLunchServings(1);
        dayOne.setDinnerServings(1);

        // Zweiter Tagesplan
        DailyMeal dayTwo = new DailyMeal();
        dayTwo.setMealDate(end);
        dayTwo.setBreakfastServings(2);
        dayTwo.setLunchServings(2);
        dayTwo.setDinnerServings(2);

        // Repository liefert beide Mealplans im Zeitraum
        when(dailyMealRepo.findByOwnerAndMealDateBetween(owner, start, end)).thenReturn(List.of(dayOne, dayTwo));

        // Service aufrufen
        Map<LocalDate, DailyMealResponseDto> result = dailyMealService.getMealPlansMapBetween(start, end, USERNAME);

        // Map muss beide Tage enthalten
        assertEquals(2, result.size());

        // Der Starttag muss korrekt gemappt sein
        assertEquals(1, result.get(start).breakfastServings());

        // Der Endtag muss korrekt gemappt sein
        assertEquals(2, result.get(end).dinnerServings());
    }

    /**
     * Prueft die Mandantentrennung beim Upsert:
     * Der Plan eines anderen Users darf nicht ueberschrieben werden, weil ausschliesslich
     * eigentuemerbezogen gesucht wird.
     */
    @Test
    void saveOrUpdateDailyMeal_assignsOwnerAndSearchesOwnerScoped() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 4, 21);
        DailyMealRequestDto input = new DailyMealRequestDto(date, null, null, null, 1, 0, 0);
        when(dailyMealRepo.findByOwnerAndMealDate(owner, date)).thenReturn(Optional.empty());
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dailyMealService.saveOrUpdateDailyMeal(input, USERNAME);

        // Assert
        ArgumentCaptor<DailyMeal> savedMeal = ArgumentCaptor.forClass(DailyMeal.class);
        verify(dailyMealRepo).save(savedMeal.capture());
        assertSame(owner, savedMeal.getValue().getOwner());
        assertFalse(savedMeal.getValue().isGlobal());
        verify(dailyMealRepo, never()).findByMealDate(any(LocalDate.class));
    }

    /**
     * Prueft, dass die Uebersicht nur die eigenen Tagesplaene liest.
     */
    @Test
    void getMealPlans_readsOnlyPlansOfTheGivenUser() {
        // Arrange
        when(dailyMealRepo.findByOwner(owner)).thenReturn(List.of());

        // Act
        List<DailyMealResponseDto> result = dailyMealService.getMealPlans(null, null, USERNAME);

        // Assert
        assertTrue(result.isEmpty());
        verify(dailyMealRepo).findByOwner(owner);
        verify(dailyMealRepo, never()).findAll();
    }
}
